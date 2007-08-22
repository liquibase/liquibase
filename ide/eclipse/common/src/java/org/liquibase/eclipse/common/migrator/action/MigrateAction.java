package org.liquibase.eclipse.common.migrator.action;

import liquibase.migrator.Migrator;

import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCDatabase;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.liquibase.eclipse.common.LiquibasePreferences;
import org.liquibase.eclipse.common.action.BaseDatabaseAction;
import org.liquibase.eclipse.common.migrator.EclipseFileOpener;

public class MigrateAction extends BaseDatabaseAction {
	
	public void run(IAction action) {
		Migrator migrator = getMigrator(LiquibasePreferences.getRootChangeLog());
		try {
			migrator.init(getSelectedConnection(getSelection()));
			migrator.migrate();
			
			((JDBCDatabase)getSelectedDatabase(getSelection())).refresh();
		} catch (Exception e) {
			IStatus status = new OperationStatus(IStatus.ERROR, LiquibasePreferences.PLUGIN_ID, 2, "Error Updating Database: "+e.getMessage(), e); 
			ErrorDialog.openError(null, "Update Error", "Error Updating Database", status);
		}
		
	}

	
}
