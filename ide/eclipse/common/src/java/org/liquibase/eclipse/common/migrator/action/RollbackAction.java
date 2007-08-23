package org.liquibase.eclipse.common.migrator.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.liquibase.eclipse.common.action.BaseDatabaseAction;
import org.liquibase.eclipse.common.migrator.wizard.RollbackWizard;

public class RollbackAction extends BaseDatabaseAction {
	
	public void run(IAction action) {
		WizardDialog dialog = new WizardDialog(getWindow().getShell(), 
				new RollbackWizard(getSelectedDatabase(getSelection()), getSelectedConnection(getSelection())));
		dialog.open();
	}	
}
