package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.AddTableWizard;

public class AddTableAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new AddTableWizard(getSelectedDatabase(selection), getSelectedSchema(selection), getSelectedConnection(selection));
	}
}
