package org.liquibase.eclipse.common.change.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.DropForeignKeyConstraintWizard;

public class DropForeignKeyConstraintAction extends BaseRefactorAction {

	@Override
	protected Wizard createWizard(ISelection selection) {
		return new DropForeignKeyConstraintWizard(getSelectedDatabase(selection), getSelectedConnection(selection), getSelectedForeignKey(selection));
	}

}
