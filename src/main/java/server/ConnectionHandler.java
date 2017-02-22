package server;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IConnectionHandler;
import iServer.IRequestHandler;
import iServer.IServer;
import iServer.IServlet;
import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import protocol.ProtocolException;

/**
 * This class is responsible for handling a incoming request
 * by creating a {@link HttpRequest} object and sending the appropriate
 * response be creating a {@link HttpResponse} object. It implements
 * {@link Runnable} to be used in multi-threaded environment.
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 */
public class ConnectionHandler implements IConnectionHandler {
	private IServer server;
	private Socket socket;
	private HttpRequest request;
	private HttpResponse response;
	boolean isClosed;
	private final int connectionNumber;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	private Map<String, IRequestHandler> handlers;
	
	public ConnectionHandler(IServer server, Socket socket, int connectionNumber) {
		this.server = server;
		this.socket = socket;
		this.handlers = new HashMap<String, IRequestHandler>();
		this.isClosed = false;
		this.connectionNumber = connectionNumber;
		logger.info(connectionStr() + "connection handler successfully constructed.");
		
	}
	/**
	 * add a handler for the given Http method - case insensitive
	 * @param method
	 * @param handler
	 */
	@Override
	public void addHandler(String method, IRequestHandler handler) {
		this.handlers.put(method.toUpperCase(), handler);
		logger.info(connectionStr() + "new handler assigned " + method.toUpperCase() + " requests.");
	}
	
	/**
	 * get the handler for the given Http method - case insensitive
	 * @param method
	 * @return
	 */
	protected IRequestHandler getHandler(String method) {
		return this.handlers.get(method.toUpperCase());
	}

	@Override
	public boolean methodSupported(String method) {
		return this.handlers.containsKey(method.toUpperCase());
	}
	
//	public void defaultConfiguration() {
//		this.handlers = new HashMap<String, IRequestHandler>();
//		addHandler(Protocol.GET, new GetHandler(this.server));
//		// TODO implement these
////		addHandler(Protocol.POST, new GetHandler(this.server));
////		addHandler(Protocol.PUT, new GetHandler(this.server));
////		addHandler(Protocol.DELETE, new GetHandler(this.server));
//	}
	
	/**
	 * @return the socket
	 */
	@Override
	public Socket getSocket() {
		return socket;
	}
	
	private void sendResponse(HttpResponse response, OutputStream outStream) {
		logger.info(connectionStr() + "sending response.");
		try {
			response.write(outStream);
			logger.info(connectionStr() + "response has been successfully sent.");
//			System.out.println(response);
		}
		catch(Exception e){
			// We will ignore this exception
			logger.error(connectionStr() + "an unknown error occured while sending the response.", e);
			e.printStackTrace();
		}
	}
	
	private void readRequest(InputStream inStream) {
		logger.info(connectionStr() + "reading the request");
		try {
			request = HttpRequest.read(inStream);
			System.out.println(request);
			if(!request.getVersion().equalsIgnoreCase(Protocol.VERSION)) {
				// TODO: Fill in the rest of the code here
				logger.warn(connectionStr() + "the request did not have a supported HTTP version.");
				response = HttpResponseFactory.create505NotSupported(Protocol.CLOSE);
			}
			logger.info(connectionStr() + "request successfuly read.");
		}
		catch(ProtocolException pe) {
			// We have some sort of protocol exception. Get its status code and create response
			// We know only two kind of exception is possible inside fromInputStream
			// Protocol.BAD_REQUEST_CODE and Protocol.NOT_SUPPORTED_CODE
			logger.error(connectionStr() + "an protocol exception occured while reading the request.", pe);
			int status = pe.getStatus();
			if(status == Protocol.BAD_REQUEST_CODE) {
				response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
			}
			
			// TODO: Handle version not supported code as well
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error(connectionStr() + "an unknown error occured while reading the request.", e);
			// For any other error, we will create bad request response as well
			response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		}
	}

	/**
	 * The entry point for connection handler. It first parses
	 * incoming request and creates a {@link HttpRequest} object,
	 * then it creates an appropriate {@link HttpResponse} object
	 * and sends the response back to the client (web browser).
	 */
	@Override
	public void run() {
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			inStream = this.socket.getInputStream();
			outStream = this.socket.getOutputStream();
		}
		catch(Exception e) {
			logger.error(connectionStr() + "unable to open socket in/out stream(S).", e);
			e.printStackTrace();
			return;
		}

		this.request = null;
		this.response = null;
		readRequest(inStream);
		
		if(response != null) {
			sendResponse(response, outStream);
			return;
		}
		
		handleAppRequest(outStream);
		
		// TODO: improve on this
		if(response == null) {
			response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
		}
		sendResponse(response, outStream);
		closeSocket();
	}
	
	public void handleAppRequest(OutputStream outStream) {
		RequestAddress addr = new RequestAddress(request.getUri());
		logger.info("addr: " + addr.getFullAddr());
		logger.info("uri: " + addr.getUri());
		logger.info("context: " + addr.getContext());
		logger.info("filename: " + addr.getFilename());
		
		IServlet servlet = server.getServlet(addr.getContext());
		
		if (servlet == null) {
			logger.info("handling request in web server mode.");
			handleRequest(outStream);
			return;
		}
		
		logger.info("handling request in App server mode.");
		IRequestHandler handler = servlet.getHandler(addr.getUri());
		response = handler.handle(request);
	}
	
	@Override
	public void handleRequest(OutputStream outStream) {
		// We reached here means no error so far, so lets process further
		try {
			if(! methodSupported(request.getMethod())) {
				logger.warn(connectionStr() + "unhandled request received");
				response = HttpResponseFactory.create400BadRequest(Protocol.CLOSE);
			} else {
				logger.info(connectionStr() + "handling " + request.getMethod().toUpperCase() + " request.");
	//			Map<String, String> header = request.getHeader();
	//			String date = header.get("if-modified-since");
	//			String hostName = header.get("host");
				IRequestHandler handler = this.getHandler(request.getMethod());
				response = handler.handle(request);
				logger.info(request.getMethod().toUpperCase() + " request successfully handled");
			}
		}
		catch(Exception e) {
			logger.error(connectionStr() + "An error occured handling the request: " + e.getMessage());
			e.printStackTrace();
		}
//		return response;
	}
	
	@Override
	public boolean isClosed() {
		return isClosed;
	}
	
	private String connectionStr() {
		return String.format("connection %d: ", connectionNumber); 
	}

	@Override
	public void closeSocket() {
		if (isClosed) { 
			logger.warn(connectionStr() + "Tried to close a socket that is already closed!");
			return;
		}
		try{
			logger.info(connectionStr() + "closing socket for connection ");
			socket.close();
			isClosed = true;
			logger.info(connectionStr() + "successfully closed socket.");
		}
		catch(Exception e){
			// We will ignore this exception
			logger.error(connectionStr() + "Exception thrown trying to close socket.", e);
			e.printStackTrace();
		} 
	}
}
