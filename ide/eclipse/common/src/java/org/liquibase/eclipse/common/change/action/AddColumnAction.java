package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.AddColumnWizard;

public class AddColumnAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new AddColumnWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedTable(selection));
	}

}
