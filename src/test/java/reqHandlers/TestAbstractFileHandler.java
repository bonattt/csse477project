package reqHandlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;

public class TestAbstractFileHandler {
	
	private static final String ROOT_PATH = "testFiles";
	
	static IServer server;
	private HttpRequest request;
	private TestHandler handler;
	
	@Before
	public void setup() throws IOException {
		// setup mock
		request = Mockito.mock(HttpRequest.class);
		when(request.getMethod()).thenReturn("HEAD");
		server = Mockito.mock(IServer.class);
		when(server.getRootDirectory()).thenReturn(ROOT_PATH);
		handler = new TestHandler(server);
		
		DirectoryArchitect dir = new DirectoryArchitect(ROOT_PATH);
		assertTrue(dir.verifyDirectoryFile());
		assertTrue(dir.verifyTestFile());
		assertTrue(dir.verifyNoFile());
	}
	
	@Test
	public void onDir() {
		when(request.getUri()).thenReturn(DirectoryArchitect.DIR_PATH);
		handler.handle(request);
		assertFalse(handler.handleFileCalled);
		assertFalse(handler.handleNotExistsCalled);
		assertTrue(handler.handleDirectoryCalled);
	}
	
	@Test
	public void onNoneFile() {
		when(request.getUri()).thenReturn(DirectoryArchitect.NONE_PATH);
		handler.handle(request);
		assertFalse(handler.handleFileCalled);
		assertTrue(handler.handleNotExistsCalled);
		assertFalse(handler.handleDirectoryCalled);
	}
	
	@Test
	public void onFileExists() {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		handler.handle(request);
		assertTrue(handler.handleFileCalled);
		assertFalse(handler.handleNotExistsCalled);
		assertFalse(handler.handleDirectoryCalled);
	}
	
}

class TestHandler extends AbstractFileHandler {

	Logger logger = LogManager.getLogger(this.getClass());
	boolean handleFileCalled, handleNotExistsCalled, handleDirectoryCalled;
	
	private IServer server;
	
	public TestHandler(IServer server) {
		this.server = server;
		handleFileCalled = false;
		handleNotExistsCalled = false;
		handleDirectoryCalled = false;
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
		handleFileCalled = true;
		return null;
	}

	@Override
	protected HttpResponse handleNotExists(HttpRequest request, String filepath) {
		handleNotExistsCalled = true;
		return null;
	}

	@Override
	protected HttpResponse handleDirectory(HttpRequest request, File file, String filepath) {
		handleDirectoryCalled = true;
		return null;
	}
	
}