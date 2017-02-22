package watchDog;

import iServer.IServer;
import iServer.IServlet;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WatchDogServer implements Runnable {

	private static final int WAIT_TIME = 100;
	private static final int DEFAULT_TICKS = 100;

	private int count;
	private int maxCount; 
	private boolean keepRunning;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public WatchDogServer() {
		this(WAIT_TIME * DEFAULT_TICKS);
	}
	
	protected WatchDogServer(int milis) {
		keepRunning = true;
		setTimeLimit(milis);
	}
	
	@Override
	public void run() {
		logger.info("starting WatchDogServer");
		while(keepRunning) {
			tick();
		}
		logger.info("WatchDogServer finished");
	}
	
	public boolean isTimedOut() {
		return count > maxCount;
	}
	
	public void reset() {
		logger.info("WatchDogServer reset");
		count = 0;
	}
	
	public void setTimeLimit(int milis) {
		logger.info("WatchDogServer time limit set to " + milis);
		count = 0;
		maxCount = (milis / WAIT_TIME);
		if (milis % WAIT_TIME != 0) {
			maxCount =+ 1;
		}
	}
	
	public synchronized void tick() {
		count += 1;
	}
	
}
