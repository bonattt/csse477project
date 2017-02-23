package watchDog;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class WatchDogApp {

	public static void main(String[] args) throws IOException, TimeoutException {
		String watch = WatchDogService.WATCHING_EXCHANGE;
		String notify = WatchDogService.NOTIFYING_EXCHANGE;
		WatchDogService service = new WatchDogService(watch, notify);
		service.setTimeLimit(5000);
		service.setupWatchedQueue();
		service.setupNotifiedQueue();
		
		service.run();
	}
}
