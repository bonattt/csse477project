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
public class DeleteHandler extends AbstractFileHandler {

	private IServer server;
	private Logger logger = LogManager.getLogger(this.getClass());	
	
	public DeleteHandler(IServer server) {
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
		HttpResponse response;
		if (file.delete()) {
			logger.info("successfully deleted file at '" + filepath + "'");
			response = HttpResponseFactory.create200OK(null, Protocol.CLOSE);
		} else {
			response = HttpResponseFactory.create304NotModified(Protocol.CLOSE);
		}
		return response;
	}

	@Override
	protected HttpResponse handleNotExists(HttpRequest request, String filepath) {
		logger.warn("file not found at '" + filepath + "'");
		return HttpResponseFactory.create404NotFound(Protocol.CLOSE);
	}

	@Override
	protected HttpResponse handleDirectory(HttpRequest request, File file, String filepath) {
		logger.info("file is directory, trying default file 'index.html'");
		File defaultFile = new File(filepath + "index.html");
		if (defaultFile.exists()) {
			return this.handleFile(request, defaultFile, filepath+"/index.html");
		} else {
			return HttpResponseFactory.create404NotFound(Protocol.CLOSE);
		}
	}

}
