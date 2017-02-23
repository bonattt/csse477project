package loadBalancing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServer;
import iServer.IServlet;
import server.ConnectionHandler;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class LoadBalancer implements IServer {

	protected static final String REQUEST_EXCHANGE = "requests";
	protected static final String RESPONSE_EXCHANGE = "responses";

	private ServerSocket welcomeSocket;
	private int port;
	private String rootDirectory;
	private AtomicBoolean stop;
	
	private int connectionNumber = 1;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public LoadBalancer(String rootDirectory, int port) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.stop = new AtomicBoolean(false);
	}
	
	@Override
	public void run() {
		try {
			this.welcomeSocket = new ServerSocket(port);
			
			// Now keep welcoming new connections until stop flag is set to true
			while(!this.stop.get()) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = this.welcomeSocket.accept();
				logger.info("receiving new connection from " + connectionSocket.getInetAddress());
				logger.info("opening on port  " + connectionSocket.getPort());
				logger.info("connection number " + connectionNumber);
				// Come out of the loop if the stop flag is set
				
				// Create a handler for this incoming connection and start the handler in a new thread
				BalanceHandler handler = constructBalanceHandler(connectionSocket);
				new Thread(handler).start();
			}
			closeAll();
		}
		catch(IOException e) {
			logger.error("I/O exception in LoadBalancer.run(): " + e.getMessage());
			logger.error(e.getStackTrace());
		}
		catch(TimeoutException e) {
			logger.error("timeout exception in LoadBalancer.run(): " + e.getMessage());
			logger.error(e.getStackTrace());
		}
		catch(Exception e) {
			logger.error("exception in LoadBalancer.run(): " + e.getMessage());
		}	
	}
	
	private BalanceHandler constructBalanceHandler(Socket socket) {
		BalanceHandler handler = new BalanceHandler(this, socket, connectionNumber++);
		return handler;
	}
	
	private void closeAll() throws IOException, TimeoutException {
		this.welcomeSocket.close();
	}

	@Override
	public int getPort() {
		return this.port;
	}

	@Override
	public String getRootDirectory() {
		return this.rootDirectory;
	}

	@Override
	public void stop() {
		this.stop.set(true);
	}

	@Override
	public boolean isStoped() {
		return this.stop.get();
	}

	@Override
	public IServlet getServlet(String uri) {
		String msg = "getServlet is not defined for class LoadBalancer";
		logger.error(msg);
		throw new UnsupportedOperationException(msg);
	}

	@Override
	public boolean addServlet(String context, Class<? extends IServlet> class1) {
		String msg = "addServlet is not defined for class LoadBalancer";
		logger.error(msg);
		throw new UnsupportedOperationException(msg);
	}
}
