package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.DropViewWizard;

public class DropViewAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new DropViewWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedView(selection));
	}
	
}
