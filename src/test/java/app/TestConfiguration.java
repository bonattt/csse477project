package app;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

public class TestConfiguration {
	
	private Configuration config;
	
	@Before
	public void setup() throws IOException {
		config = Configuration.getInstance();
		config.loadConfigs("./testConfig1");
	}

	@Test
	public void testRootFile1() throws IOException {
		String expected = "./web";
		System.out.println(config.get("root"));
		assertEquals(expected, config.get("root"));
	}

	@Test
	public void testPortFile1() throws IOException {
		String expected = "8080";
		System.out.println(config.get("port"));
		assertEquals(expected, config.get("port"));
	}

//	@Test
//	public void testRootFile2() throws IOException {
//		config.clear("root");
//		config.loadConfigs("./testConfig2");
//		String expected = "./web2";
//		System.out.println(config.get("root"));
//		assertEquals(expected, config.get("root"));
//	}
	
	@Test
	public void testClear() {
		config.clear("port");
		assertEquals(null, config.get("port"));
	}

	@Test
	public void testSetChangesValue() {
		String key = "port";
		config.set(key, "1");
		assertEquals("1", config.get(key));
		config.set(key, "2");
		assertEquals("2", config.get(key));
	}
	
	@Test
	public void testSetNewValue() {
		String key = "new value";
		config.set(key, "1");
		assertEquals("1", config.get(key));
	}

//	@Test
//	public void testPortFile2() throws IOException {
//		config.clear("port");
//		config.loadConfigs("./testConfig2");
//		String expected = "2";
//		System.out.println(config.get("port"));
//		assertEquals(expected, config.get("port"));
//	}

}
