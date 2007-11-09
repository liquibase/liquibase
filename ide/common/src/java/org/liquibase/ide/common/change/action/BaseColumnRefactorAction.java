package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BaseColumnRefactorAction extends BaseRefactorAction {

    public BaseColumnRefactorAction(String title) {
        super(title);
    }

    public abstract RefactorWizard createRefactorWizard(Column selectedColumn);

    public final RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return createRefactorWizard((Column) dbObject);
    }

    public Change[] createChanges(DatabaseObject selectedColumn, RefactorWizardPage... pages) {
        return createChanges(((Column) selectedColumn), pages);
    }

    protected abstract Change[] createChanges(Column selectedColumn, RefactorWizardPage... pages);

    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(Column.class);
    }
}
