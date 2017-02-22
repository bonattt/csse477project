package watchDog;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.Before;
import org.junit.Test;

public class TestWatchDogServer {

	private WatchDogServer server;
	
	@Before
	public void setup() {
		server = new WatchDogServer();
	}
	
	@Test
	public void testTickIncrimentsCount() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogServer.class.getDeclaredField("count");
		f.setAccessible(true);
		
		int initial = f.getInt(server);
		server.tick();
		assertEquals(initial+1, f.getInt(server));
	}
	
	@Test
	public void testSetTimeLimit() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogServer.class.getDeclaredField("maxCount");
		f.setAccessible(true);
		
		int initial = f.getInt(server);
		server.setTimeLimit(100);
		assertEquals(1, f.getInt(server));
	}

	@Test
	public void testResetZerosCount() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		
		Field f = WatchDogServer.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 1);
		
		server.reset();
		assertEquals(0, f.getInt(server));
	}
	
	@Test
	public void testIsTimedOut() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Field f = WatchDogServer.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, Integer.MAX_VALUE);
		
		assertTrue(server.isTimedOut());
	}
	
	@Test
	public void testIsNotTimedOut() 
				throws NoSuchFieldException, SecurityException,
					IllegalArgumentException, IllegalAccessException {
		Field f = WatchDogServer.class.getDeclaredField("count");
		f.setAccessible(true);
		f.setInt(server, 0);
		
		assertFalse(server.isTimedOut());
	}

}
