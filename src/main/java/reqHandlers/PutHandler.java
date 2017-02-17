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
import java.io.FileWriter;
import java.io.IOException;

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
public class PutHandler extends AbstractFileHandler {

	private IServer server;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public PutHandler(IServer server) {
		this.server = server;
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}

	@Override
	protected IServer getServer() {
		return server;
	}

	@Override
	protected HttpResponse handleFile(HttpRequest request, File file, String filepath) {
		HttpResponse response = null;
		try {
			boolean append = false;
			FileWriter writer = new FileWriter(file, append);
			writer.write(new String(request.getBody()));
			writer.close();
			response = HttpResponseFactory.create200OK(null, Protocol.CLOSE);
		} catch (IOException e) {
			logger.error("Error writting to file: " + e.getMessage());
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}

	@Override
	protected HttpResponse handleNotExists(HttpRequest request, String filepath) {
		try {
			File newFile = new File(filepath);
			newFile.createNewFile();
			logger.info(String.format("successfully create new file '%s' at '%s'",
					newFile.getName(), filepath));
			return handleFile(request, newFile, filepath); 
		} catch (IOException e) {
			logger.error("received error creating new file: " + e.getMessage());
			logger.error(String.format("failed to create new file at '%s'",
					filepath));			
			return HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
	}

	@Override
	protected HttpResponse handleDirectory(HttpRequest request, File file, String filepath) {
		logger.info("file is directory, checking for default file 'index.html'");
		File defaultFile = new File(filepath + "index.html");
		if (defaultFile.exists()) {
			return this.handleFile(request, defaultFile, filepath+"/index.html");
		} else {
			return handleNotExists(request, filepath+"/index.html");
		}
	}

}
