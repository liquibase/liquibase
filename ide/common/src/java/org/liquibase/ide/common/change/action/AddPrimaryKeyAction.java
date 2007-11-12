package org.liquibase.ide.common.change.action;

import liquibase.change.AddPrimaryKeyChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddPrimaryKeyWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddPrimaryKeyWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddPrimaryKeyAction extends BaseColumnRefactorAction {
    public AddPrimaryKeyAction() {
        super("Add Primary Key");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Make "+selectedColumn+" primary key", new AddPrimaryKeyWizardPageImpl());
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddPrimaryKeyChange change = new AddPrimaryKeyChange();
        change.setTableName(column.getTable().getName());
        change.setColumnNames(column.getName());
        change.setConstraintName(((AddPrimaryKeyWizardPage) pages[0]).getConstraintName());

        return new Change[] { change };
    }

}
