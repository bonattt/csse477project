package app;

import static org.junit.Assert.*;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.rabbitmq.client.ConnectionFactory;

import plugins.PluginMonitorException;
import watchDog.WatchDogService;

public class TestAppRestarts {

	private static SimpleWebServer server;
	private Thread thread1init, thread2init, thread3init;
	private Thread thread1, thread2, thread3;
		
	@BeforeClass
	public static void beforeClass() throws IOException, TimeoutException {
		server = SimpleWebServer.getInstance();
		server.setupMessageReceiving();
	}
	
	@Before
	public void setup() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException, 
					IOException, PluginMonitorException {

		server.setupServer();
		server.setupThreads();
		
		Field f1 = SimpleWebServer.class.getDeclaredField("thread1");
		f1.setAccessible(true);
		thread1init = (Thread) f1.get(server);

		Field f2 = SimpleWebServer.class.getDeclaredField("thread2");
		f2.setAccessible(true);
		thread2init = (Thread) f2.get(server);

		Field f3 = SimpleWebServer.class.getDeclaredField("thread3");
		f3.setAccessible(true);
		thread3init = (Thread) f3.get(server);
	}
	
	@After
	public void tearDown() {
		if (server.isRunning()) {
			server.stopApp(false);
		}
	}
	
	@Test
	public void testThread1changed() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.reboot();
		
		Field f1 = SimpleWebServer.class.getDeclaredField("thread1");
		f1.setAccessible(true);
		thread1 = (Thread) f1.get(server);
		assertNotEquals(thread1, thread1init);
	}
	
	@Test
	public void testThread2changed() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.reboot();
		
		Field f = SimpleWebServer.class.getDeclaredField("thread2");
		f.setAccessible(true);
		thread2 = (Thread) f.get(server);
		assertNotEquals(thread2, thread2init);
	}

	@Test
	public void testThread3changed() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.reboot();
		
		Field f = SimpleWebServer.class.getDeclaredField("thread3");
		f.setAccessible(true);
		thread3 = (Thread) f.get(server);
		assertNotEquals(thread3, thread3init);
	}

	@Test
	public void testAppStillRunning() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.reboot();
		assertTrue(server.isRunning());
	}

	@Test
	public void testAppStops() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.stopApp(false);
		assertFalse(server.isRunning());
	}

	@Test
	public void testThread1Stops() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.stopApp(false);

		Field f = SimpleWebServer.class.getDeclaredField("thread1");
		f.setAccessible(true);
		thread1 = (Thread) f.get(server);
		assertFalse(thread1.isAlive());
	}

	@Test
	public void testThread2Stops() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.stopApp(false);

		Field f = SimpleWebServer.class.getDeclaredField("thread2");
		f.setAccessible(true);
		thread2 = (Thread) f.get(server);
		assertFalse(thread2.isAlive());
	}

	@Test
	public void testThread3Stops() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();
		server.stopApp(false);

		Field f = SimpleWebServer.class.getDeclaredField("thread3");
		f.setAccessible(true);
		thread3 = (Thread) f.get(server);
		assertFalse(thread3.isAlive());
	}

	@Test
	public void testThread1Starts() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();

		Field f = SimpleWebServer.class.getDeclaredField("thread1");
		f.setAccessible(true);
		thread1 = (Thread) f.get(server);
	}

	@Test
	public void testThread2Starts() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();

		Field f = SimpleWebServer.class.getDeclaredField("thread2");
		f.setAccessible(true);
		thread2 = (Thread) f.get(server);
		assertTrue(thread2.isAlive());
	}

	@Test
	public void testThread3Starts() 
				throws IOException, PluginMonitorException, NoSuchFieldException,
				SecurityException, IllegalArgumentException, IllegalAccessException {
		
		server.startApp();

		Field f = SimpleWebServer.class.getDeclaredField("thread3");
		f.setAccessible(true);
		thread3 = (Thread) f.get(server);
		assertTrue(thread3.isAlive());
	}

	@Test
	public void testAppRestartsWhenCorrectKeySent()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException,
				IllegalAccessException, IOException, TimeoutException {
		server.startApp();

		
		String exchange = WatchDogService.WATCH_DOG_EXCHANGE;
		String key = "correct key";
		server.setRestartKey(key.getBytes());
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, "fanout");
		
		channel.basicPublish(exchange, "", null, key.getBytes("UTF-8"));
		
		try {
			channel.close();
		} catch (TimeoutException e) {/* do nothing */}
		connection.close();
		
		Field f = SimpleWebServer.class.getDeclaredField("thread3");
		f.setAccessible(true);
		thread3 = (Thread) f.get(server);
		
		assertNotEquals(thread3init, thread3);
	}
	
	@Test
	public void testAppDoesntRestartsForWrongKey() 
			throws NoSuchFieldException, SecurityException, IllegalArgumentException,
				IllegalAccessException, IOException, TimeoutException {
		server.startApp();

		Field f = SimpleWebServer.class.getDeclaredField("thread3");
		f.setAccessible(true);
		thread3 = (Thread) f.get(server);
		
		String exchange = WatchDogService.WATCH_DOG_EXCHANGE;
		String goodKey = "incorrect key";
		server.setRestartKey(goodKey.getBytes());
		String badKey = "correct key";
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("localhost");
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();
		channel.exchangeDeclare(exchange, "fanout");
		
		channel.basicPublish(exchange, "", null, badKey.getBytes("UTF-8"));
		
		try {
			channel.close();
		} catch (TimeoutException e) {/* do nothing */}
		connection.close();
		
		assertTrue(thread3.isAlive());
	}

}
