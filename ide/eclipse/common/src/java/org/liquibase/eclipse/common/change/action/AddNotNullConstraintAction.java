package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.AddNotNullConstraintWizard;

public class AddNotNullConstraintAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new AddNotNullConstraintWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedColumn(selection));
	}

}
