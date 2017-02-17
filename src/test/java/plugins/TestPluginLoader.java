package plugins;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IServer;

public class TestPluginLoader {

	private IServer server;
	private PluginMonitor monitor;
	private PluginClassLoader classLoader;
	
	private PluginLoader loader;
	
	@Before
	public void setup() {
		classLoader = Mockito.mock(PluginClassLoader.class);
		server =  Mockito.mock(IServer.class);
		monitor = Mockito.mock(PluginMonitor.class);
		loader = new PluginLoader(server, monitor, classLoader);
	}
	
	@Test
	public void test() {
		when(monitor.getPathWatched()).thenReturn("context");
		assertEquals("helloWorld.HelloWorld", loader.getServletClassName("context/HelloWorld.jar"));
	}

}
