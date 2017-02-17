package reqHandlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;


public class TestHeadHandler {

	private static final String EXISTING_FILE_PATH = DirectoryArchitect.FILE_PATH;
	
	private static final String ROOT_PATH = "testFiles";
	
	private static final boolean append = false;
	
	private IRequestHandler handler;
	
	private HttpRequest request;
	private IServer server;
	
	@Before
	public void setup() throws IOException {
		// setup mock
		request = Mockito.mock(HttpRequest.class);
		when(request.getMethod()).thenReturn("HEAD");
		server = Mockito.mock(IServer.class);
		when(server.getRootDirectory()).thenReturn(ROOT_PATH);
		handler = new HeadHandler(server);
		
		DirectoryArchitect dir = new DirectoryArchitect(ROOT_PATH);
		assertTrue(dir.verifyDirectoryFile());
		assertTrue(dir.verifyTestFile());
		assertTrue(dir.verifyNoFile());
	}
	
	@Test
	public void testFailsIfFileNonexistant() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.NONE_PATH);
		HttpResponse response = handler.handle(request);
		assertEquals(404, response.getStatus());
	}

	@Test
	public void testReturnsStatus200onSuceess() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		HttpResponse response = handler.handle(request);
		assertEquals(200, response.getStatus());
	}
}
