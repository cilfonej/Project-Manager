package managment.storage;

import java.io.File;

public class FileStorage {
	static final String FILE_NAME = "qc_projectManager.prjs";
	static final File ROOT_DIR = new File(System.getProperty("user.home"), ".qc_projectManager");
	
	public static final File SAVE_FILE = new File(ROOT_DIR, FILE_NAME);
	public static final File SETTINGS_FILE = new File(ROOT_DIR, "Settings.prop");
	
	static { if(!ROOT_DIR.exists()) ROOT_DIR.mkdir(); }
}
