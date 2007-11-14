package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.action.BaseDatabaseAction;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.ChangeMetaDataWizardPage;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BaseRefactorAction extends BaseDatabaseAction {
    private ChangeMetaDataWizardPage metaDataPage;

    public BaseRefactorAction(String title) {
        super(title);
    }


    public ChangeMetaDataWizardPage getMetaDataPage() {
        return metaDataPage;
    }

    public void setMetaDataPage(ChangeMetaDataWizardPage metaDataPage) {
        this.metaDataPage = metaDataPage;
    }

    public abstract RefactorWizard createRefactorWizard(DatabaseObject dbObject);

    public abstract Change[] createChanges(DatabaseObject selectedTable, RefactorWizardPage... pages);

    public boolean needsRefresh() {
        return true;
    }
}
