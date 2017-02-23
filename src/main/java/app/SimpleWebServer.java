package app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServlet;
import plugins.PluginClassLoader;
import plugins.PluginLoader;
import plugins.PluginMonitor;
import plugins.PluginMonitorException;
import server.Server;
import watchDog.WatchDogService;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 * @author Thoams Bonatti (bonattt@rose-hulman.edu)
 */
public class SimpleWebServer {
	
	public static final String EXCHANGE = WatchDogService.WATCH_DOG_EXCHANGE;
	
	private static SimpleWebServer instance;
	
	private Logger logger = LogManager.getLogger(this.getClass());
	private Thread thread1, thread2, thread3;
	private Server server;
	private PluginMonitor monitor;
	private PluginClassLoader classLoader;
	private PluginLoader loader;
	private boolean running;
	private boolean messagingSettup = false;
	private String restartKey = null;
	private String queueName;
	
	
	public static synchronized SimpleWebServer getInstance() {
		if (instance == null) {
			instance = new SimpleWebServer();
			
			
			
		}
		return instance;
	}
	
	public void setRestartKey(byte[] newKey) {
		try {
			restartKey = new String(newKey, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("could not set key because UTF-8 is not supported");
		}
	}
	
	public String getQueueName() {
		return queueName;
	}
	
	public void setupMessageReceiving() throws IOException, TimeoutException {
		if(messagingSettup) {
			logger.warn("messaging Receiving already setup");
			return;
		}
		logger.info("setting up message receiving");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(EXCHANGE, "fanout");
		
		queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE, "");
		
		Consumer consumer = new DefaultConsumer(channel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
				String message = new String(body, "UTF-8");
				logger.info("handling message " + message);
				if (message.equals(restartKey)) {
					try {
						logger.info("rebooting server app");
						reboot();
						logger.info("successfully rebooted");
					} catch (PluginMonitorException e) {
						logger.error("restart failed!");
					}
				}
			}
		};
		channel.basicConsume(queueName, true, consumer);
		logger.info("message receiving has been setup.");
		messagingSettup = true;
	}

	public void setupServer() throws IOException, PluginMonitorException {
		Configuration config = Configuration.getInstance();
		String rootDirectory = config.get(Configuration.ROOT); 
		int port = Integer.parseInt(config.get(Configuration.PORT));

		server = new Server(rootDirectory, port);
		monitor = new PluginMonitor("plugins");
		classLoader = new PluginClassLoader();
		loader = new PluginLoader(server, monitor, classLoader);
		loader.loadStartingFiles();
	}
	
	public void setupThreads() {
		thread1 = new Thread(server);
		thread2 = new Thread(monitor);
		thread3 = new Thread(loader);
	}
	
	public void startApp() {
		running = true;
		thread1.start();
		logger.info(String.format("Simple Web Server started at port %d and serving the %s directory ...%n",
				server.getPort(), server.getRootDirectory()));

		thread2.start();
		logger.info("Plugin monitor started, and watching the directory " + 
					monitor.getPathWatched());
		
		thread3.start();
		logger.info("Plugin Loader running.");
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void stopApp(boolean restarting) {
		logger.info("stopping server!");
		server.stop();
		monitor.stop();
		loader.stop();
		try {
			thread1.join();
		} catch (InterruptedException e) { /* do nothing */	}
		logger.info("thread1 joined");
		try {
			thread2.join();
		} catch (InterruptedException e) { /* do nothing */	}
		logger.info("thread2 joined");
		try {
			thread3.join();
		} catch (InterruptedException e) { /* do nothing */	}
		logger.info("thread3 joined");
		logger.info("server stopped");
		running = restarting;
	}
	
	public void reboot() throws IOException, PluginMonitorException {
		logger.info("restarting server!");
		stopApp(true);
		setupServer();
		setupThreads();
		startApp();
		logger.info("server restarted");
	}
	
	public static void main(String[] args) 
			throws IOException, PluginMonitorException {
		SimpleWebServer server = getInstance();
		server.setupServer();
		server.setupThreads();
		server.startApp();
		while(server.running) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {}
		}
	}
}
