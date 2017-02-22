package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServer;
import iServer.IServlet;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class LoadBalancer implements IServer {
	
	protected static final String CONNECTION_QUEUE_NAME = "TomSWS_connectionQueue";

	private ServerSocket welcomeSocket;
	private int port;
	private String rootDirectory;
	private AtomicBoolean stop;
	private Channel channel;
	private Connection connection;
	
	private int connectionNumber = 1;
	private Logger logger = LogManager.getLogger(this.getClass());

	public LoadBalancer(String rootDirectory, int port) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.stop = new AtomicBoolean(false);
		initializeRabbitMQ();
	}
	
	private void initializeRabbitMQ() {
		boolean durable = true;		// I'm fairly sure this one is actually correct.
		boolean passive = false;	// I'm not sure this is the correct name...
		boolean exclusive = false;	// I'm not sure this is the correct name...
//		boolean  autoDelete;
		
		logger.info("beginning RabbitMQ initialization...");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
			channel.queueDeclare(CONNECTION_QUEUE_NAME, durable, passive, exclusive, null);
		} 
		catch (IOException e) {
			logger.error("IOException initializing RabbitMQ: " + e.getMessage());
			return;
		} 
		catch (TimeoutException e) {
			logger.error("TimeoutException initializing RabbitMQ: " + e.getMessage());
			return;
		}
		
		logger.info("RabbitMQ initialization completed successfully");
	}
	
	@Override
	public void run() {
		try {
			this.welcomeSocket = new ServerSocket(port);
			
			// Now keep welcoming new connections until stop flag is set to true
			while(!this.stop.get()) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = this.welcomeSocket.accept();
				logger.info("receiving new connection from " + connectionSocket.getInetAddress());
				logger.info("opening on port  " + connectionSocket.getPort());
				logger.info("connection number " + connectionNumber);
				// Come out of the loop if the stop flag is set
				
				// Create a handler for this incoming connection and start the handler in a new thread
				logger.info("");
				forwardConnection(connectionSocket);
			}
			closeAll();
		}
		catch(IOException e) {
			logger.error("I/O exception in LoadBalancer.run(): " + e.getMessage());
			logger.error(e.getStackTrace());
		}
		catch(TimeoutException e) {
			logger.error("timeout exception in LoadBalancer.run(): " + e.getMessage());
			logger.error(e.getStackTrace());
		}
		catch(Exception e) {
			logger.error("exception in LoadBalancer.run(): " + e.getMessage());
		}	
	}
	
	private void closeAll() throws IOException, TimeoutException {
		this.welcomeSocket.close();
		this.channel.close();
		this.connection.close();
	}

	public void forwardConnection(Socket connection) throws IOException {
		// TODO implement this
		String message = "anything";
		channel.basicPublish("", CONNECTION_QUEUE_NAME,
				MessageProperties.PERSISTENT_TEXT_PLAIN,
				message.getBytes()
			);
		logger.info(String.format("message '%s' sent!", message));
		logger.warn("LoadBalancer has not fully implemented forwardConnection");
//		(this, connection, connectionNumber++);
		connectionNumber++;
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public String getRootDirectory() {
		return this.rootDirectory;
	}

	@Override
	public void stop() {
		this.stop.set(true);
	}

	@Override
	public boolean isStoped() {
		return this.stop.get();
	}

	@Override
	public IServlet getServlet(String uri) {
		String msg = "getServlet is not defined for class LoadBalancer";
		logger.error(msg);
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public boolean addServlet(String context, Class<? extends IServlet> class1) {
		String msg = "addServlet is not defined for class LoadBalancer";
		logger.error(msg);
		throw new UnsupportedOperationException(msg);
	}
}
