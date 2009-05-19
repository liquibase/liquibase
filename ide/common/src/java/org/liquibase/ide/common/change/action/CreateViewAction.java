package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.CreateViewChange;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.CreateViewWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class CreateViewAction extends BaseDatabaseRefactorAction {
    public CreateViewAction() {
        super("Create View");
    }

    @Override
    public RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return new RefactorWizard("Create View", new CreateViewWizardPageImpl());
    }

    @Override
    public Change[] createChanges(DatabaseObject selectedTable, RefactorWizardPage... pages) {
        CreateViewChange change = new CreateViewChange();
        change.setViewName(((CreateViewWizardPageImpl) pages[0]).getViewName());
        change.setSelectQuery(((CreateViewWizardPageImpl) pages[0]).getViewDefinition());

        return new Change[] { change };
    }
}
