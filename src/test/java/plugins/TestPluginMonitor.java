package plugins;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestPluginMonitor {
	
	private static final String DIR = "plugins";
	private static final String NEW_FILE = DIR + "/newFile.txt";
	private static final String DELETED_FILE = DIR + "/deleteFile.txt";
	
	private File newFile, delFile;

	private final Logger logger = LogManager.getLogger(this.getClass());
	
	private PluginMonitor mon;
	private Thread thread;
	
	@Before
	public void setup() throws IOException, PluginMonitorException {
		logger.info("starting setup.");
		newFile = new File(NEW_FILE);
		delFile = new File(DELETED_FILE);
		if (newFile.exists()) {
			logger.info("deleteing " + newFile.getAbsolutePath());
			newFile.delete();
		}
		if (!delFile.exists()) {
			logger.info("creating " + newFile.getAbsolutePath());
			delFile.createNewFile();
		}
		mon = new PluginMonitor(DIR);
		thread = new Thread(mon);
		logger.info("setup complete.");
		while (! mon.isReady()) {}
	}
	
	@After
	public void cleanUp() {
		logger.info("cleanup!");
		if (thread.isAlive()) {
			logger.info("stopping monitor in cleanup");
			mon.stop();
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.info("assertFalse(" + thread.isAlive() + ")");
				assertFalse(thread.isAlive());
			}
		}
		logger.info("cleanup complete.");
	}
	
//	@Test
//	public void testThreadStops() {
//		thread.start();
//		mon.stop();
//		try {
//			thread.join();
//		} catch (InterruptedException e) {
//			assertFalse(thread.isAlive());
//		}
//	
//	}

/*	@Test
	public void testThreadStarts() {
		thread.start();
		assertTrue(thread.isAlive());
	}
*/
	@Test
	public void testDetectsNewFile() throws IOException {
		newFile.createNewFile();
		mon.stop();
		thread.run();
		assertTrue(mon.getNextFile().equals(NEW_FILE));
		mon.stop();
	}

//	@Test
//	public void testDetectsNewFileThreaded() throws IOException {
//		thread.start();
//		newFile.createNewFile();
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {}
//		assertTrue(mon.getNextFile().equals(NEW_FILE));
//		mon.stop();
//	}
}
