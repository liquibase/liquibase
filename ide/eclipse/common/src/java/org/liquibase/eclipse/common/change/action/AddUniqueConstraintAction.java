package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.AddUniqueConstraintWizard;

public class AddUniqueConstraintAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new AddUniqueConstraintWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedColumn(selection));
	}
	
}