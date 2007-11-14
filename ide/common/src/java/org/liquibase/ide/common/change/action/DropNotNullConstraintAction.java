package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.DropNotNullConstraintChange;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropNotNullConstraintAction extends BaseColumnRefactorAction {
    public DropNotNullConstraintAction() {
        super("Drop Not Null Constraint");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Drop not null constraint on "+selectedColumn);
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        DropNotNullConstraintChange change = new DropNotNullConstraintChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setColumnDataType(column.getDataTypeString(column.getTable().getDatabase()));

        return new Change[] { change };
    }

}
