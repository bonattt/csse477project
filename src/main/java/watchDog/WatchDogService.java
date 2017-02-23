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

public class WatchDogService implements Runnable {

	private static final int WAIT_TIME = 100;
	private static final int DEFAULT_TICKS = 100;
	private static final byte[] DEFAULT_CODE = new byte[]{'a', 'b', 'c', 1, 2, 3};

	private int count, maxCount; 
	private String notificationQueue, watchedQueue;
	private boolean keepRunning;

	private Channel notifiedChannel, watchedChannel;
	private Connection notifiedConnection, watchedConnection;
	
	private String message;
	private boolean messageReceived;
	
	private byte[] notificationCode;
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public WatchDogService(String notifiedQueue, String watchedQueue)
				throws IOException, TimeoutException {
		this(notifiedQueue, watchedQueue, WAIT_TIME * DEFAULT_TICKS);
	}
	
	protected WatchDogService(String notifyQueue, String checkQueue, int milis) 
				throws IOException, TimeoutException {
		this.notificationQueue = notifyQueue;
		this.watchedQueue = checkQueue;
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
		logger.warn("this method is not implemented yet");
	}
	
	private void setupNotifiedQueue() throws IOException, TimeoutException {
		logger.info("setting up notified queue: '" + notificationQueue + "'");
		logger.warn("this method is not implemented yet");
		logger.info("notified queue: '" + notificationQueue + "' is now consuming messages.");
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
//		closeAll();
	}

	private void closeAll() {
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
	
	public boolean checkForMessages() {
		logger.info("checking for messages from " + watchedQueue);
		return false;
	}
	
	public synchronized void tick() {
		count += 1;
	}
	
}
