/*
 * Server.java
 * Oct 7, 2012
 *
 * Simple Web Server (SWS) for CSSE 477
 * 
 * Copyright (C) 2012 Chandan Raj Rupakheti
 * 
 * This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation, either 
 * version 3 of the License, or any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/lgpl.html>.
 * 
 */
 
package server;

import java.net.InetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServer;
import iServer.IServlet;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import protocol.Protocol;
import reqHandlers.DeleteHandler;
import reqHandlers.GetHandler;
import reqHandlers.HeadHandler;
import reqHandlers.PostHandler;
import reqHandlers.PutHandler;

/**
 * This represents a welcoming server for the incoming
 * TCP request from a HTTP client such as a web browser. 
 * 
 * @author Chandan R. Rupakheti (rupakhet@rose-hulman.edu)
 * @author Thomas Bonatti (bonattt@rose-hulman.edu)
 */
public class Server implements IServer {
	private String rootDirectory;
	private int port;
	private AtomicBoolean stop;
	private ServerSocket welcomeSocket;
	private Logger logger = LogManager.getLogger(this.getClass());
	private int connectionNumber = 1;
	
	private Map<String, IServlet> servletsLoaded;
	private Map<String, Class<? extends IServlet>> servletClasses;

	/**
	 * @param rootDirectory
	 * @param port
	 */
	public Server(String rootDirectory, int port) {
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.stop = new AtomicBoolean(false);
		
		servletsLoaded = new HashMap<>();
		servletClasses = new ConcurrentHashMap<>();
	}
	
	public IServlet getServlet(String uri) {
		if (! servletsLoaded.containsKey(uri)) {
			boolean success = instantiateIServlet(uri);
			if (! success) {
				logger.info("failed to instantiate new servlet...");
				return null;
			};
		}
		logger.info("returning servlet '"+uri+"'");
		return servletsLoaded.get(uri);
	}
	
	public boolean addServlet(String context, Class<? extends IServlet> class1) {
		if (servletClasses.containsKey(context)) {
			logger.warn("servlet with name '"+context+"' already exists.");
			return false;
		}
		servletClasses.put(context, class1);
		logger.info("servlet '"+context+"' successfully added to server.");
		return true;
	}
	
	public boolean instantiateIServlet(String uri) {
		logger.info("instantiating servlet '" + uri + "'");
		if (! servletClasses.containsKey(uri)) {
			logger.info("servlet does not exits");
			return false;
		}
		Class<? extends IServlet> clazz = servletClasses.get(uri);
		try {
			IServlet newServlet = clazz.newInstance();
			servletsLoaded.put(uri, newServlet);
			logger.info("successfully instantiated the servlet!");
			return true;
		} catch (InstantiationException e) {
			logger.error("InstantiationException loading Servlet " + e.getMessage());
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException loading Servlet " + e.getMessage());
		}
		return false;
	}

	/**
	 * Gets the root directory for this web server.
	 * 
	 * @return the rootDirectory
	 */
	public String getRootDirectory() {
		return rootDirectory;
	}


	/**
	 * Gets the port number for this web server.
	 * 
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * The entry method for the main server thread that accepts incoming
	 * TCP connection request and creates a {@link ConnectionHandler} for
	 * the request.
	 */
	public void run() {
		try {
			this.welcomeSocket = new ServerSocket(port);
			
			// Now keep welcoming new connections until stop flag is set to true
			while(true) {
				// Listen for incoming socket connection
				// This method block until somebody makes a request
				Socket connectionSocket = this.welcomeSocket.accept();
				logger.info("receiving new connection from " + connectionSocket.getInetAddress());
				logger.info("opening on port  " + connectionSocket.getPort());
				logger.info("connection number " + connectionNumber);
				// Come out of the loop if the stop flag is set
				if(this.stop.get())
					break;
				
				// Create a handler for this incoming connection and start the handler in a new thread
				logger.info("");
				ConnectionHandler handler = constructDefaultConnectionHandler(connectionSocket);
				new Thread(handler).start();
			}
			this.welcomeSocket.close();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private ConnectionHandler constructDefaultConnectionHandler(Socket connection) {
		ConnectionHandler handler = new ConnectionHandler(this, connection, connectionNumber++);
		handler.addHandler(Protocol.GET, new GetHandler(this));
		handler.addHandler(Protocol.HEAD, new HeadHandler(this));
		handler.addHandler(Protocol.PUT, new PutHandler(this));
		handler.addHandler(Protocol.POST, new PostHandler(this));
		handler.addHandler(Protocol.DELETE, new DeleteHandler(this));
		return handler;
	}
	
	/**
	 * Stops the server from listening further.
	 */
	public synchronized void stop() {
		if(this.stop.get())
			return;
		
		// Set the stop flag to be true
		this.stop.set(true);
		try {
			// This will force welcomeSocket to come out of the blocked accept() method 
			// in the main loop of the start() method
			Socket socket = new Socket(InetAddress.getLocalHost(), port);
			
			// We do not have any other job for this socket so just close it
			socket.close();
		}
		catch(Exception e){}
	}
	
	/**
	 * Checks if the server is stopeed or not.
	 * @return
	 */
	public boolean isStoped() {
		if(this.welcomeSocket != null)
			return this.welcomeSocket.isClosed();
		return true;
	}
}
