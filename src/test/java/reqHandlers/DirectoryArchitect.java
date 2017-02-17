package reqHandlers;

import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

class DirectoryArchitect {

	public static final String DIR_PATH = "/testDirectory";
	public static final String FILE_PATH = "/testFile.txt";
	public static final String NONE_PATH = "/nothing Should Be At This File Path At All";

	public static final String FIRST_LINE = "first line";
	public static final String LAST_LINE = "last line";
	
	private Logger logger = LogManager.getLogger(this.getClass());	
	private String root;
	
	public DirectoryArchitect(String root) {
		this.root = root;
		this.logger = logger;
		logger.info("Root: " + root);
	}
	
	public boolean verifyDirectoryFile() {
		File dirFile = new File(root + DIR_PATH);
		logger.info(root + DIR_PATH);
		if (dirFile.exists()) {
			return dirFile.isDirectory();
		}
		else {
			return false;
		}
	}

	public boolean verifyTestFile() throws IOException {
		boolean append = false;
		File txtFile = new File(root + FILE_PATH);
		logger.info(root + FILE_PATH);
		if (txtFile.exists()) {
			if (txtFile.isDirectory()) {
				return false;
			}
		}
		else {
			txtFile.createNewFile();
		}
		StringBuilder msg = new StringBuilder();
		msg.append(FIRST_LINE);
		msg.append('\n');
		msg.append(LAST_LINE);
		msg.append('\n');
		FileWriter writer = new FileWriter(txtFile, append);
		writer.write(msg.toString());
		writer.close();
		return true;
	}
	
	public boolean verifyNoFile() {
		File file = new File(root + NONE_PATH); 
		logger.info(root + NONE_PATH);
		if (file.exists()) {
			file.delete();
		}		
		return true;
	}

}
