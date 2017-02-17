package server;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IRequestHandler;
import iServer.IServer;
import iServer.IServlet;
import protocol.HttpRequest;
import protocol.HttpResponse;

public class TestConnectionHandler {

	private static final String APP_CONTEXT = "APP_URI";
	private static final String WEB_CONTEXT = "";
	private static final String APP_URI = APP_CONTEXT + "/context/dir";
	private static final String WEB_URI = WEB_CONTEXT + "/context/dir";

	private HttpRequest req;
	private HttpResponse res;
	private ConnectionHandler conHandler;
	private IServer server;
	private IServlet servlet;
	private IRequestHandler webHandler;
	private IRequestHandler appHandler;
	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;
	
	@Before
	public void setup() 
			throws IOException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		inputStream = Mockito.mock(InputStream.class);
		outputStream = Mockito.mock(OutputStream.class);
		req = Mockito.mock(HttpRequest.class);
		res = Mockito.mock(HttpResponse.class);
		server = Mockito.mock(IServer.class);
		servlet = Mockito.mock(IServlet.class);
		webHandler = Mockito.mock(IRequestHandler.class);
		appHandler = Mockito.mock(IRequestHandler.class);
		socket = Mockito.mock(Socket.class);
		
		conHandler = new ConnectionHandler(server, socket, 1);
		conHandler.addHandler("GET", webHandler);

		Field f = ConnectionHandler.class.getDeclaredField("request");
		f.setAccessible(true);
		f.set(conHandler, req);

		when(server.getServlet(WEB_CONTEXT)).thenReturn(null);
		when(server.getServlet(APP_CONTEXT)).thenReturn(servlet);
		when(servlet.getHandler(WEB_URI)).thenReturn(appHandler);
		when(webHandler.handle(req)).thenReturn(res);
		when(req.getMethod()).thenReturn("GET");
		when(socket.getInputStream()).thenReturn(inputStream);
		when(socket.getOutputStream()).thenReturn(outputStream);
	}

	@Test
	public void testAppHandledAppUri() {
		when(req.getUri()).thenReturn(APP_URI);
		conHandler.handleAppRequest(outputStream);
		verify(appHandler, times(1)).handle(req);
	}

	@Test
	public void testWebNotHandledAppUri() {
		when(req.getUri()).thenReturn(APP_URI);
		conHandler.handleAppRequest(outputStream);
		verify(webHandler, times(0)).handle(req);
	}

	@Test
	public void testAppNotHandledWebUri() {
		when(req.getUri()).thenReturn(WEB_URI);
		conHandler.handleAppRequest(outputStream);
		verify(appHandler, times(0)).handle(req);
	}

	@Test
	public void testWebHandledWebUri() {
		when(req.getUri()).thenReturn(WEB_URI);
		conHandler.handleAppRequest(outputStream);
		verify(webHandler, times(1)).handle(req);
	}

	@Test
	public void testStatus200onHandledRequest() 
			throws IllegalArgumentException, IllegalAccessException, 
				NoSuchFieldException, SecurityException {
		conHandler.handleRequest(outputStream);
		
		Field f = conHandler.getClass().getDeclaredField("response");
		f.setAccessible(true);
		HttpResponse response = (HttpResponse) f.get(conHandler);
		assertSame(res, response);
	}
	
	@Test
	public void testUnsupportedMethodReturnsStatus400() 
			throws IllegalArgumentException, IllegalAccessException, 
				NoSuchFieldException, SecurityException {
		when(req.getMethod()).thenReturn("not handled");
		conHandler.handleRequest(outputStream);
		
		Field f = conHandler.getClass().getDeclaredField("response");
		f.setAccessible(true);
		HttpResponse response = (HttpResponse) f.get(conHandler);
		assertEquals(400, response.getStatus());
	}
	
	@Test
	public void testDoesntStartClosed() {
		assertFalse(conHandler.isClosed());
	}

	@Test
	public void testCloseClosesHandler() {
		conHandler.closeSocket();
		assertTrue(conHandler.isClosed());
	}
	
}
