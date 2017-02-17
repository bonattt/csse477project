package reqHandlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

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

public class TestGetHandler {

	private static final String ROOT_PATH = "testFiles";

	private IRequestHandler handler;
	private HttpRequest request;
	private IServer server;
	
	@Before
	public void setup() throws IOException {
		// setup mock
		request = Mockito.mock(HttpRequest.class);
		when(request.getMethod()).thenReturn("GET");
		server = Mockito.mock(IServer.class);
		when(server.getRootDirectory()).thenReturn(ROOT_PATH);
		handler = new GetHandler(server);
		
		DirectoryArchitect dir = new DirectoryArchitect(ROOT_PATH);
		assertTrue(dir.verifyDirectoryFile());
		assertTrue(dir.verifyTestFile());
		assertTrue(dir.verifyNoFile());
	}
	
//	@Test
	public void testGetsFile() {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		HttpResponse res = handler.handle(request);
	}

}
