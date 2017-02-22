package watchDog;

import iServer.IServer;
import iServer.IServlet;

import java.io.IOException;
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

public class WatchDogServer implements Runnable {

	private static final int WAIT_TIME = 100;
	private static final int DEFAULT_TICKS = 100;

	private int count, maxCount; 
	private String notifiedQueue, watchedQueue;
	private boolean keepRunning;

	private Channel notifiedChannel, watchedChannel;
	private Connection notifiedConnection, watchedConnection;
	
	private String message;
	private boolean messageReceived;
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public WatchDogServer(String notifiedQueue, String watchedQueue)
				throws IOException, TimeoutException {
		this(notifiedQueue, watchedQueue, WAIT_TIME * DEFAULT_TICKS);
	}
	
	protected WatchDogServer(String notifyQueue, String checkQueue, int milis) 
				throws IOException, TimeoutException {
		this.notifiedQueue = notifyQueue;
		this.watchedQueue = checkQueue;
		keepRunning = true;
		setTimeLimit(milis);
		setupWatchedQueue();
		try {
			setupNotifiedQueue();
		} catch (IOException e) {
			logger.error("IOExcetpion: " + e.getMessage());
//			throw e;
		}
	}
	
	private void setupWatchedQueue() throws IOException, TimeoutException {
		logger.info("setting up watched queue + '" + watchedQueue + "'");
		try {
			ConnectionFactory factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    watchedConnection = factory.newConnection();
		    watchedChannel = watchedConnection.createChannel();
//		    watchedChannel.queueDeclare(watchedQueue, false, false, false, null);
		    logger.info("successfully declared queue '" + watchedQueue + "'");
		} catch (IOException e) {
			logger.info("IOException: " + e.getMessage());
		}
	}
	
	private void setupNotifiedQueue() throws IOException, TimeoutException {
		logger.info("setting up notified queue: '" + notifiedQueue + "'");
		ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    notifiedConnection = factory.newConnection();
	    notifiedChannel = notifiedConnection.createChannel();

//	    notifiedChannel.queueDeclare(notifiedQueue, false, false, false, null);

	    Consumer consumer = new DefaultConsumer(notifiedChannel) {
	      @Override
	      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
	          throws IOException {
	    	logger.info("received a message!");
	        message = new String(body, "UTF-8");
	        logger.info(String.format("received message '%s'", message));
	        reset();
	        logger.info("finished handling message '" + message + "'");
	      }
	    };
	    notifiedChannel.basicConsume(notifiedQueue, true, consumer);
		logger.info("notified queue: '" + notifiedQueue + "' is now consuming messages.");
	}
	
	@Override
	public void run() {
		logger.info("starting WatchDogServer");
		while(keepRunning) {
			checkForMessages();
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

		try {
			notifiedChannel.close();
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
		logger.info("notifying queue " + notifiedQueue);
		
	}
	
	public boolean checkForMessages() {
		logger.info("checking for messages from " + watchedQueue);
		return false;
	}
	
	public synchronized void tick() {
		count += 1;
	}
	
}
