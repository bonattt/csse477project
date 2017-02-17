package integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IServer;
import plugins.PluginClassLoader;
import plugins.PluginLoader;
import plugins.PluginMonitor;
import plugins.PluginMonitorException;
import server.Server;

public class TestServerLoadsNewClass {

	private static final String PLUGINS = "testPlugins";
	private static final String SRC_JAR = "testFiles/test.jar";
	private static final String TEST_JAR = PLUGINS + "/test.jar";
	private static final String SERVLET_CONTEXT = TEST_JAR;

	private IServer server;
	private PluginMonitor monitor;
	private PluginClassLoader classLoader;
	
	private PluginLoader loader;
	
	private Thread tServer, tMonitor, tLoader;
	
	@Before
	public void setup() throws IOException, PluginMonitorException {
		fail("not implemented yet");
		server = new Server("web", 8080);
		monitor = new PluginMonitor(PLUGINS);
		classLoader = Mockito.mock(PluginClassLoader.class);
		
		loader = new PluginLoader(server, monitor, classLoader);
		
		tServer = new Thread(server);
		tMonitor = new Thread(monitor);
		tLoader = new Thread(loader);
		
		tServer.start();
		tMonitor.start();
		tLoader.start();
	}
	
	@After
	public void cleanup() {
		server.stop();
		monitor.stop();
		loader.stop();
	}
	
//	@Test TODO
	public void testLoadsNewClass() {
		File srcFile = new File(SRC_JAR);
		File newJar = new File(TEST_JAR);
//		FileUtils.copyDirectory(srcFile, newJar);
		assertTrue(null != server.getServlet(SERVLET_CONTEXT));
	}

}
