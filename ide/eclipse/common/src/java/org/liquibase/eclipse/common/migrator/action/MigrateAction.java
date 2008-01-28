package org.liquibase.eclipse.common.migrator.action;

import liquibase.Liquibase;
import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCDatabase;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.liquibase.eclipse.common.LiquibasePreferences;
import org.liquibase.eclipse.common.action.BaseDatabaseAction;

public class MigrateAction extends BaseDatabaseAction {
	
	public void run(IAction action) {
		try {
            Liquibase liquibase = getLiquibase(LiquibasePreferences.getRootChangeLog(), getSelectedConnection(getSelection()));
			liquibase.update(null);
			
			((JDBCDatabase)getSelectedDatabase(getSelection())).refresh();
		} catch (Exception e) {
			IStatus status = new OperationStatus(IStatus.ERROR, LiquibasePreferences.PLUGIN_ID, 2, "Error Updating Database: "+e.getMessage(), e); 
			ErrorDialog.openError(null, "Update Error", "Error Updating Database", status);
		}
		
	}

	
}
