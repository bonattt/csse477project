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

	private static String watchedQueue = "Watched Queue";
	private static String notifiedQueue = "Notified Queue";
	private static final String DEFAULT_MSG = "nothing received";
	
	private static Channel notifiedChannel, watchedChannel;
	private static Connection notifiedConnection, watchedConnection;

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
		
		server = new WatchDogService(notifiedQueue, watchedQueue);
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
		logger.warn("setup watched queue unimplemented");
	}
	
	private static void setupNotifiedQueue() throws IOException, TimeoutException {
		logger.warn("setup notified queue unimplemented");
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

	@Test(timeout=2000)
	public void testReceivesNewMessage() throws UnsupportedEncodingException, IOException {
		String sent = "hello";
//		watchedChannel.basicPublish("", watchedQueue, null, sent.getBytes("UTF-8"));
		assertTrue(server.checkForMessages());
	}

	@Test(timeout=2000)
	public void testReceivesCorrectMessage() throws UnsupportedEncodingException, IOException {
		fail("unimplemented");
		String sent = "hello";
//		watchedChannel.basicPublish("", watchedQueue, null, sent.getBytes("UTF-8"));
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

	@Test(timeout=2000)
	public void testNoNewMessages() {
		assertFalse(server.checkForMessages());
	}

	@Test(timeout=2000)
	public void testReceivingMessageResetsCount()
				throws NoSuchFieldException, SecurityException, 
					IllegalArgumentException, IllegalAccessException, 
					UnsupportedEncodingException, IOException {
		Field f = WatchDogService.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 10);
		
//	    watchedChannel.basicPublish("", watchedQueue, null, message.getBytes("UTF-8"));
	    assertEquals(0, f.getInt(server));
	}

}
