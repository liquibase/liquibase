package org.liquibase.ide.common.change.action;

import liquibase.change.AddForeignKeyConstraintChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddForeignKeyConstraintWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddForeignKeyConstraintWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddForeignKeyConstraintAction extends BaseColumnRefactorAction {
    public AddForeignKeyConstraintAction() {
        super("Add Foreign Key");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add foreign key constraint to "+selectedColumn, new AddForeignKeyConstraintWizardPageImpl());
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();
        change.setBaseTableName(column.getTable().getName());
        change.setBaseColumnNames(column.getName());

        change.setConstraintName(((AddForeignKeyConstraintWizardPage) pages[0]).getConstraintName());

        change.setReferencedTableName(((AddForeignKeyConstraintWizardPage) pages[0]).getReferencedTableName());
        change.setReferencedColumnNames(((AddForeignKeyConstraintWizardPage) pages[0]).getReferencedColumnNames());

        change.setDeleteCascade(((AddForeignKeyConstraintWizardPage) pages[0]).getDeleteCascade());
        change.setDeferrable(((AddForeignKeyConstraintWizardPage) pages[0]).getDeferrable());
        change.setInitiallyDeferred(((AddForeignKeyConstraintWizardPage) pages[0]).getInitiallyDeferred());

        return new Change[] { change };
    }
}