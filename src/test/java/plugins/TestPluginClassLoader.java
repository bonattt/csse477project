package plugins;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import iServer.IServlet;
import protocol.HttpRequest;
import protocol.HttpResponse;

public class TestPluginClassLoader {

	private Logger logger = LogManager.getLogger(this.getClass());
	private PluginClassLoader loader;
	
//	@Before
//	public void setup() throws IOException {
//		loader = new PluginClassLoader();
//		loader.setJarFile("testFiles/testPlugins/EchoPlugin.jar");
//		loader.loadAllClasses();
//	}
//	
//	@Test
//	public void testLoadsAClass() {
//		logger.info("testLoadsAClass");
//		assertFalse(null == loader.retrieveClass("echoPlugin.EchoPlugin"));
//	}
//	
//	@Test
//	public void testClassHasCorrectName() {
//		String name = "echoPlugin.EchoPlugin";
//		assertEquals(name, loader.retrieveClass(name).getName());
//	}
//
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testCanInstantiateLoadedClass() 
//				throws InstantiationException, IllegalAccessException {
//		String name = "echoPlugin.EchoPlugin";
//		Class<? extends IServlet> clazz = loader.retrieveClass(name);
//		IServlet servlet = clazz.newInstance();
//		assertFalse(servlet == null);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testInstantiatedClassMethod() 
//				throws InstantiationException, IllegalAccessException {
//		String name = "echoPlugin.EchoPlugin";
//		Class<? extends IServlet> clazz = loader.retrieveClass(name);
//		IServlet servlet = clazz.newInstance();
//		String str = servlet.getPreferedContextName();
//		assertEquals("echo", str);
//	}
//	
//	@SuppressWarnings("unchecked")
//	@Test
//	public void testInstantiatedClassMethodAdv() 
//				throws InstantiationException, IllegalAccessException {
//		String name = "echoPlugin.EchoPlugin";
//		Class<? extends IServlet> clazz = loader.retrieveClass(name);
//		IServlet servlet = clazz.newInstance();
//		HttpRequest request = Mockito.mock(HttpRequest.class);
//		when(request.getBody()).thenReturn("hello world".toCharArray());
//		HttpResponse res = servlet.getHandler("").handle(request);
//		assertEquals(200, res.getStatus());	
//	}

}
