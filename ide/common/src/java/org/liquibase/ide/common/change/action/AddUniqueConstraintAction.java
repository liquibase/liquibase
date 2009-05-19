package org.liquibase.ide.common.change.action;

import liquibase.change.AddUniqueConstraintChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class AddUniqueConstraintAction extends BaseColumnRefactorAction {
    public AddUniqueConstraintAction() {
        super("Add Unique Constraint");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Make "+selectedColumn+" unique", new SingleValueWizardPageImpl("Unique Constraint Name"));
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(column.getTable().getName());
        change.setColumnNames(column.getName());
        change.setConstraintName(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }

}
