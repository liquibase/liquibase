package org.liquibase.ide.common.change.action;

import liquibase.change.AddNotNullConstraintChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class AddNotNullConstraintAction extends BaseColumnRefactorAction {
    public AddNotNullConstraintAction() {
        super("Add Not Null Constraint");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add not null constraint to "+selectedColumn, new SingleValueWizardPageImpl("Set Existing Null Values To"));
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setColumnDataType(column.getDataTypeString(column.getTable().getDatabase()));
        change.setDefaultNullValue(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }
}