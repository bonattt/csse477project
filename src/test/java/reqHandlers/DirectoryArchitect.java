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
		logger.info("Root: " + root);
	}
	
	public boolean verifyDirectoryFile() {
		logger.info("dir-file path: " + root + DIR_PATH);
		File dirFile = new File(root + DIR_PATH);
		if (dirFile.exists()) {
			logger.info("file exists");
			if (dirFile.isDirectory()) {
				logger.info("file is already a directory");
				return true;
			} else {
				logger.info("file is not a directory");
				dirFile.delete();
			}
		}
		logger.info("file does not exist");
		return createDir(dirFile);
	}

	private boolean createDir(File dirFile) {
		if (dirFile.mkdirs()) {
			logger.info("successfully replaced file with new directory");
			return true;
		}
		logger.error("failed to replace file with new directory");
		return false;
	}
	
	public boolean verifyTestFile() {
		logger.info("exists-file path: " + root + FILE_PATH);
		boolean append = false;
		File txtFile = new File(root + FILE_PATH);
		if (txtFile.exists()) {
			if (txtFile.isDirectory()) {
				logger.info("file exists, trying to delete it.");
				try {
					txtFile.delete();
					txtFile.createNewFile();
				} catch (IOException e) {
					logger.error("failed to replace dir with new file.");
					return false;
				}
			}
		}
		else {
			try {
				txtFile.createNewFile();
			} catch (IOException e) {
				logger.error("failed to create a new file");
				return false;
			}
		}
		StringBuilder msg = new StringBuilder();
		msg.append(FIRST_LINE);
		msg.append('\n');
		msg.append(LAST_LINE);
		msg.append('\n');
		try {
			FileWriter writer = new FileWriter(txtFile, append);
			writer.write(msg.toString());
			writer.close();
		} catch (IOException e) { 
			logger.error("failed to write to file.");
			return false;
		}
		return true;
	}
	
	public boolean verifyNoFile() {
		logger.info("no-file path: " + root + NONE_PATH);
		File file = new File(root + NONE_PATH); 
		if (file.exists()) {
			file.delete();
		}
		return true;
	}

}
