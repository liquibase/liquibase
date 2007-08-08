package org.liquibase.eclipse.common.migrator.action;

import liquibase.migrator.Migrator;

import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCDatabase;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.liquibase.eclipse.common.LiquibasePreferences;
import org.liquibase.eclipse.common.action.BaseDatabaseAction;
import org.liquibase.eclipse.common.change.wizard.CreateIndexWizard;
import org.liquibase.eclipse.common.migrator.dialog.TagDatabaseDialog;
import org.liquibase.eclipse.common.migrator.wizard.RollbackWizard;

public class RollbackAction extends BaseDatabaseAction {
	
	public void run(IAction action) {
		WizardDialog dialog = new WizardDialog(getWindow().getShell(), 
				new RollbackWizard(getSelectedDatabase(getSelection()), getSelectedConnection(getSelection())));
		dialog.open();
	}	
}
