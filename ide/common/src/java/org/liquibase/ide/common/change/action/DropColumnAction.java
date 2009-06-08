package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.core.DropColumnChange;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropColumnAction extends BaseColumnRefactorAction {
    public DropColumnAction() {
        super("Drop Column");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Drop "+selectedColumn);
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        DropColumnChange change = new DropColumnChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());

        return new Change[] { change };
    }

}
