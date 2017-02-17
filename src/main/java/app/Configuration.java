package app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

import java.io.InputStream;
import java.io.OutputStream;

public class Configuration {
	
	private static Configuration instance;
	private Properties properties;

	private static final String DEFAULT_FILEPATH =
			"C:/Users/bonattt/Documents/School/02-Winter/csse-477/finalProject"
			+ "/workspace/TomSWS/configfile.txt";
	
	public static final String ROOT = "root";
	public static final String DEFAULT_ROOT_VALUE = "web";
	
	public static final String PORT = "port";
	public static final String DEFAULT_PORT_VALUE = "8080";

	public static final String NAME = "name";
	public static final String DEFAULT_NAME_VALUE = "TomSWS";

	public static final String VERSION = "version";
	public static final String DEFAULT_VERSION_VALUE = "1.0.0";
		
	
	public static Configuration getInstance() {
		if (instance == null) {
			try {
				instance = new Configuration();
				instance.setup(DEFAULT_FILEPATH);
			} catch (IOException e) {
				// TODO log the fault!
				instance.setToDefaultConfig();
			}
			
		}
		return instance;
	}
	
	public void clear(String key) {
		this.properties.remove(key);
	}
	
	public void set(String key, String value) {
		properties.setProperty(key, value);		
	}
	
	public String get(String key) {
		return properties.getProperty(key);
	}
	
	protected void setup(String filepath) throws IOException {
		this.properties = new Properties();
		loadConfigs(filepath);
	}

	public boolean saveConfigs(String filepath) throws IOException {
		return saveConfigs(filepath, true);
	}
	
	public boolean saveConfigs(String filepath, boolean forceSave) throws IOException {
		File file = new File(filepath);
		if (file.exists() && (!forceSave)) {
			// log failed to save b/c file already exists.
			return false;
		} else if (! file.exists()) {
			if (! file.createNewFile()) {
				return false;
			}
		}
		FileWriter fileIn = new FileWriter(file, false); // append = false;
		properties.store(fileIn, filepath);
		fileIn.close();
		
		return true;
	}
	
	public void loadConfigs(String filepath) throws IOException {
		File file = new File(filepath);
		if(file.exists()) {
			InputStream input = new FileInputStream(file);
			properties.load(input);
			input.close();
		} else {
			boolean success = file.createNewFile();
			if (! success) {
				throw new IOException("unable to create new config file at '" + filepath + "'");
			}
			setToDefaultConfig();
			OutputStream output = new FileOutputStream(file);
			properties.store(output, filepath);
			output.close();
		}
	}
	
	protected void setToDefaultConfig() {
		properties.setProperty(ROOT, DEFAULT_ROOT_VALUE);
		properties.setProperty(PORT, DEFAULT_PORT_VALUE);
	}
	
//	public void generateConfigs(String filepath) {
//		
//		Properties props = new Properties();
//		props.setProperty("root", DEFAULT_ROOT_VALUE);
//		props.setProperty("port", DEFAULT_PORT_VALUE);
//		
//		File file = new File(filepath);
//		
//		try {
//			if(! file.exists()) {
//				file.createNewFile();
//			}
//			FileOutputStream out = new FileOutputStream(file);
//			props.store(out, "descriptions are for the weak...");
//			out.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
	
}
