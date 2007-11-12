package org.liquibase.ide.common.change.action;

import liquibase.change.AddNotNullConstraintChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddNotNullConstraintWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddNotNullConstraintWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddNotNullConstraintAction extends BaseColumnRefactorAction {
    public AddNotNullConstraintAction() {
        super("Add Not Null Constraint");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add not null constraint to "+selectedColumn, new AddNotNullConstraintWizardPageImpl());
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddNotNullConstraintChange change = new AddNotNullConstraintChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setColumnDataType(column.getTypeName());
        change.setDefaultNullValue(((AddNotNullConstraintWizardPage) pages[0]).getDefaultNullValue());

        return new Change[] { change };
    }
}