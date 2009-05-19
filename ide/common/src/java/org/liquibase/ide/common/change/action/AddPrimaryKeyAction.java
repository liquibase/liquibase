package org.liquibase.ide.common.change.action;

import liquibase.change.AddPrimaryKeyChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class AddPrimaryKeyAction extends BaseColumnRefactorAction {
    public AddPrimaryKeyAction() {
        super("Add Primary Key");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Make "+selectedColumn+" primary key", new SingleValueWizardPageImpl("Constraint Name"));
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(column.getTable().getName());
        change.setColumnNames(column.getName());
        change.setConstraintName(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }

}
