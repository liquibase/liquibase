package org.liquibase.eclipse.common;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class LiquibasePreferences {

	public static final String PLUGIN_ID = "org.liquibase";

	public static final String PREFERENCES_ID = "org.liquibase";
	
	public static final String CURRENT_CHANGE_LOG_FILE_NAME = "currentChangeLogFileName";

	public static final String CLASSPATHS = "classpaths";
	
	public static String getCurrentChangeLogFileName() {
		IEclipsePreferences preferences = new InstanceScope().getNode(PREFERENCES_ID);
		return preferences.get(LiquibasePreferences.CURRENT_CHANGE_LOG_FILE_NAME, "~/changelog.xml");
	}

	public static void setCurrentChangeLogFileName(String fileName) {
		IEclipsePreferences preferences = new InstanceScope().getNode(PREFERENCES_ID);
		preferences.put(LiquibasePreferences.CURRENT_CHANGE_LOG_FILE_NAME, fileName);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getCurrentChangeLog() {
		Set<File> roots = getRoots();
		
		File currentFile = new File(getCurrentChangeLogFileName());
		File parentDir = currentFile.getParentFile();
		
		File root = null;
		while (parentDir != null) {
			if (roots.contains(parentDir)) {
				root = parentDir;
			}
			
			parentDir = parentDir.getParentFile();
		}

		if (root == null) { //didn't find a parent {
			return currentFile.getName();
		} else {
			return currentFile.toString().substring(root.toString().length()+1).replaceAll("\\\\", "/");
		}
	}
	
	public static void setClassPaths(String classPaths) {
		IEclipsePreferences preferences = new InstanceScope().getNode(PREFERENCES_ID);
		preferences.put(LiquibasePreferences.CLASSPATHS, classPaths);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}		
	}
	
	public static String getClassPaths() {
		IEclipsePreferences preferences = new InstanceScope().getNode(PREFERENCES_ID);
		return preferences.get(LiquibasePreferences.CLASSPATHS, null);
	}
	
	public static Set<File> getRoots() {
		Set<File> returnSet = new HashSet<File>();
		for (String file : getClassPaths().split(";")) {
			returnSet.add(new File(file));
		}
		
		return returnSet;
	}

}
