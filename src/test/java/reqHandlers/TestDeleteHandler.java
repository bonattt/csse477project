package reqHandlers;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;

public class TestDeleteHandler {

	private static final String ROOT_PATH = "testFiles";
	private static final String EXISTING_FILE_PATH = DirectoryArchitect.FILE_PATH;
	private static final String NEW_FILE_PATH = DirectoryArchitect.NONE_PATH;
	private static final String STARTING_DATA = DirectoryArchitect.FIRST_LINE;
	
	private static final boolean append = false;
	
	private IRequestHandler handler;
	
	private HttpRequest request;
	private IServer server;
	
	@Before
	public void setup() throws IOException {
		// setup mock
		request = Mockito.mock(HttpRequest.class);
		when(request.getMethod()).thenReturn("DELETE");
		server = Mockito.mock(IServer.class);
		when(server.getRootDirectory()).thenReturn(ROOT_PATH);
		handler = new DeleteHandler(server);
		
		DirectoryArchitect dir = new DirectoryArchitect(ROOT_PATH);
		assertTrue(dir.verifyDirectoryFile());
		assertTrue(dir.verifyTestFile());
		assertTrue(dir.verifyNoFile());
	}

	@Test
	public void testDeletesExistingFile() throws IOException {
		when(request.getUri()).thenReturn(EXISTING_FILE_PATH);
		HttpResponse response = handler.handle(request);
		
		File file = new File(EXISTING_FILE_PATH);
		assertFalse(file.exists());
	}
	
	@Test
	public void testFailsIfFileNonexistant() throws IOException {
		when(request.getUri()).thenReturn(NEW_FILE_PATH);
		HttpResponse response = handler.handle(request);
		assertEquals(404, response.getStatus());
	}
	
//	@Test
	public void testReturnsStatus304onFailure() throws IOException {
		when(request.getUri()).thenReturn(EXISTING_FILE_PATH);
		Scanner scanner = new Scanner(new File(EXISTING_FILE_PATH));
		HttpResponse response = handler.handle(request);
		scanner.close();
		assertEquals(304, response.getStatus());
	}

	@Test
	public void testReturnsStatus200onSuceess() throws IOException {
		when(request.getUri()).thenReturn(EXISTING_FILE_PATH);
		HttpResponse response = handler.handle(request);
		assertEquals(200, response.getStatus());
	}
}
