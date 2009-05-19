package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.DropColumnChange;
import liquibase.change.DropDefaultValueChange;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropDefaultValueAction extends BaseColumnRefactorAction {
    public DropDefaultValueAction() {
        super("Drop Default Value");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Drop default value on "+selectedColumn);
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        DropDefaultValueChange change = new DropDefaultValueChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());

        return new Change[] { change };
    }

}
