package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.liquibase.eclipse.common.action.BaseDatabaseAction;

public abstract class BaseRefactorAction extends BaseDatabaseAction {

	public BaseRefactorAction() {
		super();
	}

	protected abstract Wizard createWizard(ISelection selection);

	public void run(IAction action) {
		WizardDialog dialog = new WizardDialog(getWindow().getShell(), createWizard(getSelection()));
		dialog.open();
	}
}