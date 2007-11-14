package org.liquibase.ide.common.change.action;

import liquibase.change.AddAutoIncrementChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import liquibase.database.Database;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddAutoIncrementAction extends BaseColumnRefactorAction {
    public AddAutoIncrementAction() {
        super("Add Auto Increment");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Make "+selectedColumn+" auto-increment");
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setColumnDataType(column.getDataTypeString(column.getTable().getDatabase()));

        return new Change[] { change };
    }
}
