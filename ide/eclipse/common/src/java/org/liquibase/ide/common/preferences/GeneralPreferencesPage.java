package org.liquibase.ide.common.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.PathEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.liquibase.eclipse.common.LiquibasePreferences;

public class GeneralPreferencesPage extends FieldEditorPreferencePage  implements IWorkbenchPreferencePage {

	private ScopedPreferenceStore preferences;

	public static final String ID = "org.liquibase.preferences.liquibase";
	
	public GeneralPreferencesPage() {
		preferences = new ScopedPreferenceStore(new InstanceScope(), LiquibasePreferences.PLUGIN_ID);
		setPreferenceStore(preferences);
		
	}
	
//	@Override
	public void init(IWorkbench workbench) {		
	}

	@Override
	protected void createFieldEditors() {
		PathEditor classpathEditor = new PathEditor(LiquibasePreferences.CLASSPATHS, "Change Log Root Directories", "Select Directory", getFieldEditorParent());
		addField(classpathEditor);
		
		FileFieldEditor changeLogEditor = new FileFieldEditor(LiquibasePreferences.CURRENT_CHANGE_LOG_FILE_NAME, "Current Change Log File", getFieldEditorParent());
		addField(changeLogEditor);

	}
	
	@Override
	public boolean performOk() {
//		try {
//			preferences.save();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return super.performOk();
	}

}
