package org.liquibase.ide.common.change.action;

import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import liquibase.change.Change;
import liquibase.database.structure.Table;
import liquibase.database.structure.DatabaseObject;

public abstract class BaseTableRefactorAction extends BaseRefactorAction {

    public BaseTableRefactorAction(String title) {
        super(title);
    }


    public Change[] createChanges(DatabaseObject selectedTable, RefactorWizardPage... pages) {
        return createChanges(((Table) selectedTable), pages);
    }

    protected abstract Change[] createChanges(Table selectedTable, RefactorWizardPage... pages);
}
