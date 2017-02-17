package server;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IServer;
import iServer.IServlet;

public class TestServer {
	
	private static final String TEST = "test";
	
	private IServer server;
	private IServlet servlet;
	private Class<Server> clazz = Server.class;
	
	@SuppressWarnings("unchecked")
	@Before
	public void setup() 
			throws NoSuchFieldException, SecurityException, 
				IllegalArgumentException, IllegalAccessException {
		server = new Server("testFiles", 8080);
		servlet = Mockito.mock(IServlet.class);
		Field f = clazz.getDeclaredField("servletClasses");
		f.setAccessible(true);
		Map<String, Class<IServlet>> map = (Map<String, Class<IServlet>>) f.get(server);
		map.put(TEST, (Class<IServlet>) servlet.getClass());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetUnloadedServletLoadsNewServlet() 
			throws NoSuchFieldException, SecurityException,
				IllegalArgumentException, IllegalAccessException {
		
		Field f = clazz.getDeclaredField("servletsLoaded");
		f.setAccessible(true);
		Map<String, IServlet> map = (Map<String, IServlet>) f.get(server);
		assertFalse(map.containsKey(TEST));
		server.getServlet(TEST);
		assertTrue(map.containsKey(TEST));
	}
}
