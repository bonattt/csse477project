package watchDog;

import static org.junit.Assert.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class TestWatchDogServer {

//	private static final String WATCHED_QUEUE = "WatchedQueue";
//	private static final String NOTIFIED_QUEUE = "NotifiedQueue";
	private static final String DEFAULT_MSG = "nothing received";
	private static final String WATCH_EXCH = WatchDogService.WATCHING_EXCHANGE;
	private static final String NOTE_EXCH = WatchDogService.NOTIFYING_EXCHANGE;
	
	private static String notificationQueue, watchedQueue;
	private static Channel notificationChannel, watchedChannel;
	private static Connection notificationConnection, watchedConnection;

	private WatchDogService server;
	private static String message;
	private static boolean messageReceived;
	
	private static Logger logger = LogManager.getLogger(TestWatchDogServer.class);
	
	@Before
	public void setup() throws IOException, TimeoutException {
		message = DEFAULT_MSG;
		messageReceived = false;
		
		setupWatchedQueue();
		setupNotifiedQueue();
		
		server = new WatchDogService(WATCH_EXCH, NOTE_EXCH);
		server.setupWatchedQueue();
		server.setupNotifiedQueue();
	}
	
	@After
	public void tearDown() {
		server.stop();
	}
	
//	@AfterClass
//	public void tearDown() throws IOException, TimeoutException {
//		try {
//			notifiedChannel.close();
//		} catch (AlreadyClosedException e) {/* do nothing */}
//		try {
//			watchedChannel.close();
//		} catch (AlreadyClosedException e) {/* do nothing */}
//		try {
//			notifiedConnection.close();
//		} catch (AlreadyClosedException e) {/* do nothing */}
//		try {
//			watchedConnection.close();
//		} catch (AlreadyClosedException e) {/* do nothing */}
//	}
	
	private static void setupWatchedQueue() throws IOException, TimeoutException {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		watchedConnection = factory.newConnection();
		watchedChannel = watchedConnection.createChannel();
		watchedChannel.exchangeDeclare(WATCH_EXCH, "fanout");
//		watchedQueue = watchedChannel.queueDeclare().getQueue();
		// TODO shouldn't there be something here?
	}
	
	private static void setupNotifiedQueue() throws IOException, TimeoutException {
		logger.info("setting up basic consumer.");
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		notificationConnection = factory.newConnection();
		notificationChannel = notificationConnection.createChannel();
		notificationChannel.exchangeDeclare(WATCH_EXCH, "fanout");
		
		notificationQueue = notificationChannel.queueDeclare().getQueue();
		notificationChannel.queueBind(notificationQueue, WATCH_EXCH, "");
		
		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
		Consumer consumer = new DefaultConsumer(notificationChannel) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope,
						AMQP.BasicProperties properties, byte[] body) throws IOException {
				messageReceived = true;
				logger.info("handling delivery");
				message = new String(body, "UTF-8");
				logger.info("message: '" + message + "'");
			}
		};
		notificationChannel.basicConsume(notificationQueue, true, consumer);
		logger.info("basic consumer set up");
	}

//	@Test
//	public void testHandleNewMessageSetsNewMessageFalse()
//			throws NoSuchFieldException, SecurityException,
//				IllegalArgumentException, IllegalAccessException {
//		Field f = server.getClass().getDeclaredField("newMessageReceived");
//		f.setAccessible(true);
//		f.setBoolean(server, true);
//		
//		server.handleNewMessage(new byte[]{});
//		assertFalse(f.getBoolean(server));
//	}
	
	@Test
	public void testCorrectNotificationQueueName() 
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException {
		Field f = server.getClass().getDeclaredField("notificationQueue");
		f.setAccessible(true);
		assertNotNull( f.get(server));
		assertEquals(notificationQueue, f.get(server));
	}
	
//	@Test
	public void testCorrectWatchedQueueName() 
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException {
		Field f = server.getClass().getDeclaredField("watchedQueue");
		f.setAccessible(true);
		assertNotNull(f.get(server));
		assertEquals(watchedQueue, f.get(server));
	}
	
	@Test
	public void testTickIncrimentsCount() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		
		int initial = f.getInt(server);
		server.tick();
		assertEquals(initial+1, f.getInt(server));
	}
	
	@Test
	public void testSetTimeLimit() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogService.class.getDeclaredField("maxCount");
		f.setAccessible(true);
		
		int initial = f.getInt(server);
		server.setTimeLimit(100);
		assertEquals(1, f.getInt(server));
	}

	@Test
	public void testResetZerosCount() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 1);
		
		server.reset();
		assertEquals(0, f.getInt(server));
	}
	
	@Test
	public void testIsTimedOut() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, Integer.MAX_VALUE);
		
		assertTrue(server.isTimedOut());
	}

	@Test
	public void testIsNotTimedOut() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 0);
		
		assertFalse(server.isTimedOut());
	}

	@Test(timeout=1000)
	public void testWatchDogStopsWhenStopped() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Thread t = new Thread(server);
		t.start();
		server.stop();
	}

	@Test
	public void testCheckForMessage() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 0);
		
		assertFalse(server.isTimedOut());
	}

//	@Test(timeout=2000)
//	public void testReceivesNewMessage() throws UnsupportedEncodingException, IOException {
//		String sent = "hello";
////		watchedChannel.basicPublish("", watchedQueue, null, sent.getBytes("UTF-8"));
//		assertTrue(server.hasNewMessage());
//	}

	@Test(timeout=2100)
	public void testReceivesCorrectMessage()
				throws UnsupportedEncodingException, IOException,
					IllegalArgumentException, IllegalAccessException,
					NoSuchFieldException, SecurityException {
		
		String sent = "hello";
		watchedChannel.basicPublish(WATCH_EXCH, "", null, sent.getBytes());
		
		Field f = server.getClass().getDeclaredField("lastMessage");
		f.setAccessible(true);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {/* do nothing */}
		assertEquals(sent, f.get(server));
	}
	
	@Test(timeout=2000)
	public void testNotifyAppSendsMsg()  {
		server.notifyApp();
		while(! messageReceived) {
			// busy wait.
		}
		assertNotEquals(DEFAULT_MSG, message);
	}

	@Test (timeout=2000)
	public void testNotifyAppSendsCorrectMsg()  {
		String expected = "";
		server.notifyApp();
		while(! messageReceived) {
			// busy wait.
		}
		assertEquals(expected, message);
	}

//	@Test(timeout=2000)
//	public void testNoNewMessages() {
//		assertFalse(server.hasNewMessage());
//	}

	@Test(timeout=2100)
	public void testReceivingMessageResetsCount()
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException, 
					UnsupportedEncodingException, IOException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 10);
		watchedChannel.basicPublish(WATCH_EXCH, "", null, new byte[]{});

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {/* do nothing */}
		
	    assertEquals(0, f.getInt(server));
	}
	
	@Test(timeout=2000)
	public void testHandlingSetsLastMessage()
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException, 
					UnsupportedEncodingException, IOException {
		Field f = WatchDogService.class.getDeclaredField("lastMessage");
		f.setAccessible(true);
		server.handleNewMessage("hello".getBytes());
	    assertEquals("hello", f.get(server));
	}

	@Test(timeout=2000)
	public void testHandlingMessageResetsCount()
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException, 
					UnsupportedEncodingException, IOException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 10);
		server.handleNewMessage(new byte[]{});
	    assertEquals(0, f.getInt(server));
	}

	@Test(timeout=2000)
	public void testTimeoutTriggersMessage() {
		server.setTimeLimit(300);
		Thread t = new Thread(server);
		t.start();
		while(! messageReceived) { /* busy wait */ }
		// message was received, test should pass.
	}

//	@Test(timeout=2000)
	public void testSendsCorrectCode() {
		logger.info("before:   " + server.getNotificationCode());
		byte[] sent = "hello".getBytes();
		server.setNotificationCode(sent);
		logger.info("after:    " + server.getNotificationCode());
		server.notifyApp();
		logger.info("received: " + message.getBytes());
		assertEquals(sent, message.getBytes());
	}

}
