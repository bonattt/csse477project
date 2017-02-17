package reqHandlers;

import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import app.Configuration;
import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

public class HeadHandler extends AbstractFileHandler { //implements IRequestHandler {

	private IServer server;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	public HeadHandler(IServer server) {
		this.server = server;
		logger.info("HEAD request handler constructed");
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
		HttpResponse response = HttpResponseFactory.create200OK(null, Protocol.CLOSE);
		Configuration config = Configuration.getInstance();
		String filename = file.getName();
		logger.info("found file " + filename);
		response.put("Content-Length", "" + file.getTotalSpace());
		String contentType = AbstractFileHandler.getContentType(filename);
		if (contentType != null) {
			response.put("Content-Type", contentType);
		}
		response.put("Server", config.get(Configuration.NAME) + ": version" +
					config.get(Configuration.VERSION));
		response.put("filename", filename);
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
