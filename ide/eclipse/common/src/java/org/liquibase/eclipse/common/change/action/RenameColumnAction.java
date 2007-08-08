package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.RenameColumnWizard;

public class RenameColumnAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new RenameColumnWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedColumn(selection));
	}

}
