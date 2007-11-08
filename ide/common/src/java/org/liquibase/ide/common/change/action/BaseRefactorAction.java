package org.liquibase.ide.common.change.action;

import org.liquibase.ide.common.action.BaseDatabaseAction;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.ChangeMetaDataWizardPage;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.change.Change;

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
}
