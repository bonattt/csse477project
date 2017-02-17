package reqHandlers;

import java.io.File;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;
import protocol.HttpResponseFactory;
import protocol.Protocol;

public abstract class AbstractFileHandler implements IRequestHandler {
	
	private static final HashMap<String, String> contentTypes;
	
	static {
		contentTypes = new HashMap<>();
		contentTypes.put(".html", "text/html");
		contentTypes.put(".json", "application/json");
		contentTypes.put(".txt", "text/plain");
		contentTypes.put(".xml", "text/xml");
		contentTypes.put(".js", "application/javascript");
		contentTypes.put(".css", "stylesheet/css");
	}
	
	public static String getContentType(String filename) {
		String extension = filename.substring(filename.lastIndexOf('.'));
		if (contentTypes.containsKey(extension)) {
			return contentTypes.get(extension);
		}
		return null;		
	}
	
	public static String getContentType(File file) {
		return getContentType(file.getName());
	}
	
	@Override
	public HttpResponse handle(HttpRequest request) {
		getLogger().info("request being handled!");
		HttpResponse response = null;
		String uri = request.getUri();
		String rootDirectory = getServer().getRootDirectory();
		File file = new File(rootDirectory + uri);
		getLogger().info("local path: '" + rootDirectory + uri + "'");
		getLogger().info("absolute path: '" + file.getAbsolutePath());
		if(file.exists()) {
			if(file.isDirectory()) {
				response = handleDirectory(request, file, rootDirectory+uri);
			}
			else { // Its a file
				response = handleFile(request, file, rootDirectory+uri);
			}
		}
		else {
			response = handleNotExists(request, rootDirectory+uri);
		}
		getLogger().info("finished handling request");
		return response;
	}

	protected abstract Logger getLogger();
	protected abstract IServer getServer();

	protected abstract HttpResponse handleFile(HttpRequest request, File file, String filepath);

	protected abstract HttpResponse handleNotExists(HttpRequest request, String filepath);

	protected abstract HttpResponse handleDirectory(HttpRequest request, File file, String filepath);

}
