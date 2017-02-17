package server;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestRequestAddress {

	private static final String ADDRESS = "/context/uri/filename";
	private static final String FILENAME = "filename";
	private static final String CONTEXT = "context";
	private static final String URI = "/uri/filename";
	
	private RequestAddress addr;
	private String requestStr;

	@Test
	public void testGetFullAddr() {
		addr = new RequestAddress(ADDRESS);
		assertEquals(ADDRESS, addr.getFullAddr());
	}

	@Test
	public void testGetFullAddrAppendsSlash() {
		addr = new RequestAddress(ADDRESS.substring(1));
		assertEquals(ADDRESS, addr.getFullAddr());
	}

	@Test
	public void testGetUri() {
		addr = new RequestAddress(ADDRESS);
		assertEquals(URI, addr.getUri());
	}

	@Test
	public void testGetContext() {
		addr = new RequestAddress(ADDRESS);
		assertEquals(CONTEXT, addr.getContext());
	}

	@Test
	public void testGetFilename() {
		addr = new RequestAddress(ADDRESS);
		assertEquals(FILENAME, addr.getFilename());
	}

	@Test
	public void testGetFilenameShortContext() {
		addr = new RequestAddress("hello");
		assertEquals("", addr.getFilename());
	}

	@Test
	public void testGetUriShortContext() {
		addr = new RequestAddress("hello");
		assertEquals("", addr.getUri());
	}

	@Test
	public void testGetContextShortContext() {
		addr = new RequestAddress("hello");
		assertEquals("hello", addr.getContext());
	}

	@Test
	public void testGetFullAddrShortContext() {
		addr = new RequestAddress("hello");
		assertEquals("/hello", addr.getFullAddr());
	}

}
