/*
 * GetHandler.java
 * Feb 1, 2017
 *
 * Copyright (C) 2015 Chandan Raj Rupakheti
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
 
package reqHandlers;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;
import server.Server;

/**
 * 
 * @author Thomas Bonatti (bonattt@rose-hulman.edu)
 */
public class GetHandler implements IRequestHandler {

	private IServer server;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public GetHandler(IServer server) {
		this.server = server;
		logger.info("GET request handler constructed");
	}
	
	/* (non-Javadoc)
	 * @see server.IRequestHandler#handleRequest(protocol.HttpRequest)
	 */
	@Override
	public HttpResponse handle(HttpRequest request) {
		logger.info("GET request being handled!");
		HttpResponse response = null;
		String uri = request.getUri();
		String rootDirectory = server.getRootDirectory();
		File file = new File(rootDirectory + uri);
		logger.info("local path: '" + rootDirectory + uri + "'");
		logger.info("absolute path: '" + file.getAbsolutePath());
		if(file.exists()) {
			if(file.isDirectory()) {
				logger.info("request for directory");
				// Look for default index.html file in a directory
				String location = rootDirectory + uri + System.getProperty("file.separator") + Protocol.DEFAULT_FILE;
				file = new File(location);
				if(file.exists()) {
					response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
					logger.info("response generated with index.html from requested directory");
				}
				else {
					logger.warn("requested directory does not contain index.html");
					response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
				}
			}
			else { // Its a file
				logger.info("request for file at '" + rootDirectory + uri + "'");
				response = HttpResponseFactory.create200OK(file, Protocol.CLOSE);
				logger.info("response generated with requested file");
			}
		}
		else {
			logger.warn("requested location does not exist.");
			response = HttpResponseFactory.create404NotFound(Protocol.CLOSE);
		}
		logger.info("finished handling GET request");
		return response;
	}

}
