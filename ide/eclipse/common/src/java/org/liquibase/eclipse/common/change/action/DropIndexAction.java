package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.DropIndexWizard;

public class DropIndexAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new DropIndexWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedIndex(selection));
	}

}