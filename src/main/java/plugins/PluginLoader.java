package plugins;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServer;
import iServer.IServlet;

public class PluginLoader implements Runnable {
	
	private IServer server;
	private PluginMonitor monitor;
	private AtomicBoolean keepRunning;
	private PluginClassLoader loader;
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public PluginLoader(IServer server, PluginMonitor monitor, PluginClassLoader loader) {
		keepRunning = new AtomicBoolean(true);
		this.server = server;
		this.monitor = monitor;
		this.loader = loader;
	}
	
	public void loadStartingFiles() {
		loadNewFile("", monitor.getPathWatched());
	}
	
	private void loadNewFile(String filepath, String filename) {
		logger.info("loading new file '" + filepath + '/' + filename + "'");
		File newFile = new File(addFilenameToFilepath(filepath, filename));
		if (newFile.isDirectory()) {
			logger.info("new file is a directory.");
			loadNewDirectory(filepath, filename);
			return;
		}
		filepath = addFilenameToFilepath(filepath, filename);
		logger.info("file '"+ filepath +"'is not a directory");
		Class<? extends IServlet> clazz = loadServlet(filepath);
		if (clazz != null) {
			String context = getAppContext(filename);
			server.addServlet(context, clazz);
			logger.info("successfully added servlet '" + context + "'");
		} else {
			logger.error("servlet class was null!");
		}
	}
	
	private void loadNewDirectory(String filepath, String filename) {
		filepath = addFilenameToFilepath(filepath, filename);
		logger.info("loading from dir '" + filepath + "'");
		File file = new File(filepath);
		String[] allFiles = file.list();
		logger.info(allFiles);
		for (String str : allFiles) {
			loadNewFile(filepath, str);
			logger.info("done loading file '" + str + "'");
		}
	}
	
	public String addFilenameToFilepath(String filepath, String filename) {
		if (filepath.equals("")) {
			return filename;
		} else {
			return filepath + '/' + filename;
		}
	}
	
	
	public void run() {
		while(keepRunning.get()) {
			if (monitor.hasNextFile()) {
				String filename = monitor.getNextFile();
				loadNewFile("", filename);
			} else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// do nothing
				}
			}
		}
	}
	
	private String getAppContext(String filename) {
		int start = filename.lastIndexOf('/');
		int end = filename.lastIndexOf('.');
		return filename.substring(start+1, end);
	}
			
	
	public void stop() {
		if (keepRunning.get()) {
			logger.info("stopping plugin loader");
			keepRunning.set(false);
		}
		else {
			logger.warn("plugin loader already stopped");
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<? extends IServlet> loadServlet(String filename) {
		logger.info("loading servlet from file '" + filename + "'");
		loader.setJarFile(filename);
		try {
			loader.loadAllClasses();
		} catch (IOException e) {
			logger.error("failed to load classes from the the jar file\n" + e.getMessage());
			return null;
		}
		String className = getServletClassName(filename);
		logger.info("trying to load servlet named '" + className + "'");
		return (Class<? extends IServlet>) loader.retrieveClass(className);
	}
	
	public String getServletClassName(String filename) {
		int index = filename.lastIndexOf('/')+1;
		filename = filename.substring(index);
		filename = filename.substring(0, filename.length()-4);
		String firstChar = "" + filename.charAt(0);
		filename = filename.replaceFirst(firstChar, firstChar.toLowerCase()) +
				'.' + filename;
		return filename;
	}
	
}
