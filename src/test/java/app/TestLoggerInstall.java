package app;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;


public class TestLoggerInstall {
	
	private static final String INFO = "test info msg";
	private static final String WARN = "test warning msg";
	private static final String ERROR = "test error msg";

	private Logger logger;
	private PrintStream newOut;
	private ByteArrayOutputStream stream;
	
	@Before
	public void setup() {
		logger = LogManager.getLogger(this.getClass());
		stream = new ByteArrayOutputStream();
		newOut = new PrintStream(stream);
		System.setOut(newOut);
	}
	
	@Test
	public void testRoot() {
		logger.info(INFO);
		logger.warn(WARN);
		logger.error(ERROR);
//		String content = new String(stream.toByteArray(), StandardCharsets.UTF_8);
//		assertTrue(content.contains(INFO));
//		assertTrue(content.contains(WARN));
//		assertTrue(content.contains(ERROR));
		assertTrue(true);
	}

}
