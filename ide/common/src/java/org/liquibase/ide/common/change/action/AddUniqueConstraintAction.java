package org.liquibase.ide.common.change.action;

import liquibase.change.AddUniqueConstraintChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddUniqueConstraintWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddUniqueConstraintWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddUniqueConstraintAction extends BaseColumnRefactorAction {
    public AddUniqueConstraintAction() {
        super("Add Unique Constraint");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Make "+selectedColumn+" unique", new AddUniqueConstraintWizardPageImpl());
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddUniqueConstraintChange change = new AddUniqueConstraintChange();
        change.setTableName(column.getTable().getName());
        change.setColumnNames(column.getName());
        change.setConstraintName(((AddUniqueConstraintWizardPage) pages[0]).getConstraintName());

        return new Change[] { change };
    }

}
