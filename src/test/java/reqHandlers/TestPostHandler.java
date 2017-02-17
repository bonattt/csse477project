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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IRequestHandler;
import iServer.IServer;
import protocol.HttpRequest;
import protocol.HttpResponse;

public class TestPostHandler {

	private static final String EXISTING_FILE_PATH = DirectoryArchitect.FIRST_LINE;
	private static final String NEW_FILE_PATH = "postTest02.txt";
	private static final String STARTING_DATA = "this file already exists. This line should NOT be overwritten\n";
	private static final String WRITING_DATA = "this is the new data that should be written\n";
	private static final String ROOT_PATH = "testFiles";
	
	private static final boolean append = false;
	
	private IRequestHandler handler;
	private HttpRequest request;
	private IServer server;
	
	@Before
	public void setup() throws IOException {
		// setup mock
		request = Mockito.mock(HttpRequest.class);
		when(request.getMethod()).thenReturn("POST");
		when(request.getBody()).thenReturn(WRITING_DATA.toCharArray());
		
		server = Mockito.mock(IServer.class);
		
		when(server.getRootDirectory()).thenReturn(ROOT_PATH);
		handler = new PostHandler(server);
		
		DirectoryArchitect dir = new DirectoryArchitect(ROOT_PATH);
		assertTrue(dir.verifyDirectoryFile());
		assertTrue(dir.verifyTestFile());
		assertTrue(dir.verifyNoFile());
	}
	
//	@Test
	public void testWritesToNewFile() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.NONE_PATH);
		HttpResponse response = handler.handle(request);
		
		Scanner scanner = new Scanner(new File(NEW_FILE_PATH));
		String filetext = "";
		while(scanner.hasNextLine()) {
			filetext += scanner.nextLine();
		}
		scanner.close();
		assertTrue(filetext.contains(WRITING_DATA));
	}

//	@Test
	public void testWritesToExistingFile() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		HttpResponse response = handler.handle(request);
		
		Scanner scanner = new Scanner(new File(EXISTING_FILE_PATH));
		String filetext = "";
		while(scanner.hasNextLine()) {
			filetext += scanner.nextLine();
		}
		scanner.close();
		assertTrue(filetext.contains(WRITING_DATA));
	}
	
//	@Test
	public void testCreatesNewFile() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.NONE_PATH);
		when(request.getBody()).thenReturn((ROOT_PATH + NEW_FILE_PATH).toCharArray());
		HttpResponse response = handler.handle(request);
		File newFile = new File(NEW_FILE_PATH);
		assertTrue(newFile.exists());
	}
	
//	@Test
	public void testAppendsToFile() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		HttpResponse response = handler.handle(request);
		
		Scanner scanner = new Scanner(new File(EXISTING_FILE_PATH));
		String filetext = "";
		while(scanner.hasNextLine()) {
			filetext += scanner.nextLine();
		}
		scanner.close();
		assertTrue(filetext.contains(STARTING_DATA));
	}

	@Test
	public void testReturnsStatus200onSuceess() throws IOException {
		when(request.getUri()).thenReturn(DirectoryArchitect.FILE_PATH);
		HttpResponse response = handler.handle(request);
		assertEquals(200, response.getStatus());
	}
}
