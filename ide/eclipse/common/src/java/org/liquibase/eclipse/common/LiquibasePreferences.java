package org.liquibase.eclipse.common;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

public class LiquibasePreferences {

	public static final String PLUGIN_ID = "org.liquibase";

	public static final String PREFERENCES_ID = "org.liquibase";
	
	public static final String CURRENT_CHANGE_LOG_FILE_NAME = "currentChangeLogFileName";
	
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
		return getCurrentChangeLogFileName().replaceAll(".*\\\\","");
	}

}
