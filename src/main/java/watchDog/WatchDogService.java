package watchDog;

import iServer.IServer;
import iServer.IServlet;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class WatchDogService implements Runnable {

	public static final String WATCH_DOG_EXCHANGE = "watchDogExchange";
	
	private static final int WAIT_TIME = 100;
	private static final int DEFAULT_TICKS = 100;
	private static final byte[] DEFAULT_CODE = new byte[]{'a', 'b', 'c', 1, 2, 3};

	private int count, maxCount; 
	private String notificationQueue, watchedQueue, exchangeName;
	private boolean keepRunning;

	private Channel notificationChannel, watchedChannel;
	private Connection notifiedConnection, watchedConnection;
	
	private String lastMessage;
	
	private byte[] notificationCode;
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public WatchDogService(String notifiedQueue, String watchedQueue, String exchangeName)
				throws IOException, TimeoutException {
		this(notifiedQueue, watchedQueue, exchangeName, WAIT_TIME * DEFAULT_TICKS);
	}
	
	protected WatchDogService(String notifyQueue, String checkQueue, String exchangeName, int milis) 
				throws IOException, TimeoutException {
		this.notificationQueue = notifyQueue;
		this.watchedQueue = checkQueue;
		this.exchangeName = exchangeName;
		this.notificationCode = DEFAULT_CODE;
		this.keepRunning = true;
		
		setTimeLimit(milis);
		setupWatchedQueue();
		try {
			setupNotifiedQueue();
		} catch (IOException e) {
			logger.error("IOExcetpion: " + e.getMessage());
//			throw e;
		}
	}
	
	public void setNotificationCode(byte[] newCode) {
		notificationCode = newCode;
	}
	
	private void setupWatchedQueue() throws IOException, TimeoutException {
		logger.info("setting up watched queue + '" + watchedQueue + "'");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		watchedConnection = factory.newConnection();
		watchedChannel = watchedConnection.createChannel();
		watchedChannel.exchangeDeclare(exchangeName, "fanout");
		
		watchedQueue = watchedChannel.queueDeclare().getQueue();
		watchedChannel.queueBind(watchedQueue, exchangeName, "");
		
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		Consumer consumer = new DefaultConsumer(watchedChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
				handleNewMessage(body);
			}
		};
		watchedChannel.basicConsume(watchedQueue, true, consumer);
	}
	
	private void setupNotifiedQueue() throws IOException, TimeoutException {
		logger.info("setting up notified queue: '" + notificationQueue + "'");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		notifiedConnection = factory.newConnection();
		notificationChannel = notifiedConnection.createChannel();
		notificationChannel.exchangeDeclare(exchangeName, "fanout");
		
		notificationChannel.queueBind(notificationQueue, exchangeName, "");
		
		
		logger.info("notified queue: '" + notificationQueue + "' is now ready.");
	}
	
	@Override
	public void run() {
		logger.info("starting WatchDogServer");
		while(keepRunning) {
			if (isTimedOut()) {
				logger.warn("watch dog timed out!");
				notifyApp();
			}
			try {
				Thread.sleep(WAIT_TIME);
			} catch (InterruptedException e) { /* do nothing */ }
			tick();
		}
		logger.info("WatchDogServer finished");
		closeAll();
	}

	private void closeAll() {
		try {
			notificationChannel.close();
		} catch (Exception e) {
			logger.warn("Exception thrown closing notifiedChannel");
			logger.info(e + " : msg = " + e.getMessage());
		}

		try {
			watchedChannel.close();
		} catch (Exception e) {
			logger.warn("Exception thrown closing watchedChannel");
			logger.info(e + " : msg = " + e.getMessage());
		}

		try {
			notifiedConnection.close();
		} catch (Exception e) {
			logger.warn("Exception thrown closing notifiedConnection");
			logger.info(e + " : msg = " + e.getMessage());
		}

		try {
			watchedConnection.close();
		} catch (Exception e) {
			logger.warn("Exception thrown closing watchedConnection");
			logger.info(e + " : msg = " + e.getMessage());
		}
		logger.info("closed connections and channels.");
	}
	
	public boolean isTimedOut() {
		return count > maxCount;
	}
	
	public void reset() {
		logger.info("WatchDogServer reset");
		count = 0;
	}
	
	public void setTimeLimit(int milis) {
		logger.info("WatchDogServer time limit set to " + milis);
		count = 0;
		maxCount = (milis / WAIT_TIME);
		if (milis % WAIT_TIME != 0) {
			maxCount =+ 1;
		}
	}
	
	public synchronized void stop() {
		logger.info("stopping watch dog server!");
		if (!keepRunning) {
			logger.warn("Watch Dog Server already stopped!");
		}
		keepRunning = false;
	}
	
	public void notifyApp() {
		logger.info("notifying queue " + notificationQueue);
	    logger.info(String.format("sending code %s", notificationCode));
//	    try {
////			notifiedChannel.basicPublish("", notificationQueue, null, notificationCode);
//		} catch (IOException e) {
//			logger.error("Failed to send notification in queue '" + notificationQueue + "'");
//			logger.error("IO Exception: " + e.getMessage());
//		} catch (Exception e) {
//			logger.error("error: " + e.getMessage());
//		}
	}
	
	public synchronized void handleNewMessage(byte[] msg) {
		try {
			lastMessage = new String(msg, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("UTF-8 encoding not supported");
			lastMessage = "utf-8 not supported";
		}
		count = 0;
	}
	
	public synchronized void tick() {
		count += 1;
	}
	
}
