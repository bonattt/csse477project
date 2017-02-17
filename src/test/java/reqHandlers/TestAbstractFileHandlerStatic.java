package reqHandlers;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class TestAbstractFileHandlerStatic {

	private static final String JSON = "fake.json";
	private static final String TXT = "fake.txt";
	private static final String HTML = "fake.html";
	private static final String XML = "fake.xml";
	private static final String JS = "fake.js";
	private static final String CSS = "fake.css";

	
	
	@Test
	public void testJsonFile() {
		File file = new File(JSON);
		assertEquals("application/json", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testJsonPath() {
		assertEquals("application/json", AbstractFileHandler.getContentType(JSON));
	}

	@Test
	public void testTxtFile() {
		File file = new File(TXT);
		assertEquals("text/plain", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testTxtPath() {
		assertEquals("text/plain", AbstractFileHandler.getContentType(TXT));
	}

	@Test
	public void testHtmlFile() {
		File file = new File(HTML);
		assertEquals("text/html", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testHtmlPath() {
		assertEquals("text/html", AbstractFileHandler.getContentType(HTML));
	}

	@Test
	public void testXmlFile() {
		File file = new File(XML);
		assertEquals("text/xml", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testXmlPath() {
		assertEquals("text/xml", AbstractFileHandler.getContentType(XML));
	}

	@Test
	public void testJsFile() {
		File file = new File(JS);
		assertEquals("application/javascript", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testJsPath() {
		assertEquals("application/javascript", AbstractFileHandler.getContentType(JS));
	}

	@Test
	public void testFile() {
		File file = new File(CSS);
		assertEquals("stylesheet/css", AbstractFileHandler.getContentType(file));
	}

	@Test
	public void testPath() {
		assertEquals("stylesheet/css", AbstractFileHandler.getContentType(CSS));
	}

}
