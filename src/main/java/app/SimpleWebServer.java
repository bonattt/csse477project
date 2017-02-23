package app;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServlet;
import plugins.PluginClassLoader;
import plugins.PluginLoader;
import plugins.PluginMonitor;
import plugins.PluginMonitorException;
import server.Server;

/**
 * The entry point of the Simple Web Server (SWS).
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 * @author Thomas Bonatti (bonattt@rose-hulman.edu)
 */
public class SimpleWebServer {
	
	private static Logger logger = LogManager.getLogger(SimpleWebServer.class);
	
	public static void main(String[] args) 
			throws InterruptedException, IOException, PluginMonitorException {
		Configuration config = Configuration.getInstance();
		String rootDirectory = config.get(Configuration.ROOT); 
		int port = Integer.parseInt(config.get(Configuration.PORT));

		// Create a run the server
		Server server = new Server(rootDirectory, port);
		
		PluginMonitor monitor = new PluginMonitor("plugins");
		PluginClassLoader classLoader = new PluginClassLoader();
		PluginLoader loader = new PluginLoader(server, monitor, classLoader);
		loader.loadStartingFiles();
		
		Thread thread1 = new Thread(server);
		thread1.start();
		logger.info(String.format("Simple Web Server started at port %d and serving the %s directory ...%n",
				port, rootDirectory));

		Thread thread2 = new Thread(monitor);
		thread2.start();
		logger.info("Plugin monitor started, and watching the directory " + monitor.getPathWatched());
		
		Thread thread3 = new Thread(loader);
		thread3.start();
		logger.info("Plugin Loader running.");
		
		
		thread1.join();
		thread2.join();
		thread3.join();
	}
}
