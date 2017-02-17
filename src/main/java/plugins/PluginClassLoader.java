package plugins;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import iServer.IServlet;

public class PluginClassLoader extends ClassLoader {

	private String pathToJar = null;
	private Logger logger = LogManager.getLogger(this.getClass());
	
	@SuppressWarnings("rawtypes")
	private Map<String, Class> classesLoaded = new HashMap<>();
	
	public void setJarFile(String pathToJar) {
		this.pathToJar = pathToJar;
	}
	
	@SuppressWarnings("rawtypes")
	public Class retrieveClass(String className) {
		Class clazz = classesLoaded.get(className);
		if (clazz == null) {
			logger.warn("trying to get class "+className+" failed.");
		}
		return clazz;
	}
	
	@SuppressWarnings("rawtypes")
	public void loadAllClasses() throws IOException {
		if (pathToJar == null) {
			logger.error("PluginClassLoader has not been given a filepath to load a .jar file from.");
			return;
		}
		logger.info("loading all classes in file '" + pathToJar + "'");
		JarFile jarFile = new JarFile(pathToJar);
		Enumeration<JarEntry> enumr = jarFile.entries();
		URL[] urls = { new URL("jar:file:" + pathToJar+"!/") };
		URLClassLoader cl = URLClassLoader.newInstance(urls);
		while (enumr.hasMoreElements()) {
			try {
				JarEntry je = enumr.nextElement();
			    if(je.isDirectory() || !je.getName().endsWith(".class")){
			        continue;
			    }
			    // -6 because of .class
			    String className = je.getName().substring(0,je.getName().length()-6);
			    logger.info("Trying to load class '" + je.getName() + "'");
			    className = className.replace('/', '.');
			    Class c = cl.loadClass(className);
			    
			    if (c != null) {
				    classesLoaded.put(c.getName(), c);
//				    logger.info("successfully loaded class " + c.getName());
			    }
			} catch (ClassNotFoundException e1) {
				logger.warn("Class not found exception: " + e1.getMessage());
			}
			
		}
		jarFile.close();
	}
}
