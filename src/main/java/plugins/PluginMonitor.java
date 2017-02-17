package plugins;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PluginMonitor implements Runnable {
	
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private Path directory;
	private String context;
	private WatchService watcher;
	private WatchKey key;
	private Queue<String> newFiles;
	
	private Map<WatchKey, Path> keys;
	// use ENTRY_CREATE, and ENTRY_DELETE 
	
	private AtomicBoolean keepWatching = new AtomicBoolean(true);
    private AtomicBoolean ready = new AtomicBoolean(false);
	
	public PluginMonitor(String dir) throws IOException, PluginMonitorException {
		logger.info("constructing PluginMonitor");
        File file = makeFile(dir);
        this.context = dir;
        this.directory = file.toPath();
        newFiles = new LinkedList<String>();
        
        this.watcher = FileSystems.getDefault().newWatchService();
        this.key = directory.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        
        ready.set(true);
	}
	
	public String getPathWatched() {
		return context;
	}
	
	private File makeFile(String dir) throws IOException, PluginMonitorException {
        File file = new File(dir);
        if (file.isDirectory()) {
        	logger.info("the file '" + dir + "'is an existing directory");
        } else if (file.exists()) {
        	logger.error("the file '" + dir + "' is not a directory.");
        	throw new PluginMonitorException("the file '" + dir + "' is not a directory.");
        } else {
        	logger.info("the file '" + dir + "' does not exist.");
        	if (file.mkdir()) {
        		logger.info("created a new directory " + dir);
        	} else {
        		logger.error("failed to create the directory '" + dir + "'");
        		throw new PluginMonitorException("failed to create the directory '" + dir + "'");
        	}
        }
        return file;
	}
	
	public boolean isReady() {
		return ready.get();
	}
	
	public synchronized void stop() {
		if (keepWatching.get()) {
			logger.info("Stopping PluginMonitor");
		} else {
			logger.warn("PluginMonitor already stopped!");
		}
		keepWatching.set(false);
	}

	public void handleCreate() {
		
	}
	
	public synchronized String getNextFile() {
		String newFile =  newFiles.poll();
		logger.info("get next file '" + newFile + "'");
		return newFile;
	}
	
	public synchronized boolean hasNextFile() {
		boolean hasNext;
		if (this.newFiles.peek() == null) {
			hasNext = false;
		} else {
			hasNext = true;
		}
		return hasNext;
	}
 
	public List<String> getNewFiles() {
		ArrayList<String> ls = new ArrayList<>();
		while (hasNextFile()) {
			ls.add(this.getNextFile());
		}
		return ls;
	}
	
	public void run() {
		logger.info("starting PluginMonitor");
		
		do {
			logger.info("monitoring...");
			WatchKey newKey;
			try {
				newKey = watcher.take();
				handleChange(newKey);
				logger.info("finished handling change");
			} catch (InterruptedException e) {
				logger.error("interrupt exception while trying to get watch directory.\n\t" +
							e.getMessage());
			}		
		} while(keepWatching.get());
		// This is for testing purposes, so the loop can be run a single time outside a thread
		logger.info("exiting PluginMonitor run method");
	}
	
	private synchronized void addNewFile(String filename) {
		logger.info("adding new file '" + this.context + "/" + filename +"'");
		newFiles.add(this.context + "/" + filename);
	}
	
	private void handleChange(WatchKey newKey) {
		for (WatchEvent<?> event : newKey.pollEvents()) {
			logger.info("hanlding a WatchEvent!");
			if (event.kind() == ENTRY_CREATE) {
				logger.info("watcher create event!");
				logger.info(event.context().toString() + " was added");
				addNewFile(event.context().toString());
			}
			else if (event.kind() == ENTRY_DELETE) {
				logger.info("watcher delete event!");
				
			}				
			else if (event.kind().equals(ENTRY_MODIFY)) {
				logger.info("watcher modify event");
				
			}
			else if (event.kind().equals(OVERFLOW)) {
				logger.info("watcher overflow event");
				
			}
			else {
				logger.warn("watcher event not recognized");
				
			}
		}
	}
	
}
