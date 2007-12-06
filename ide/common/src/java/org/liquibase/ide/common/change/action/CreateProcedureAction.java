package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.CreateProcedureChange;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageTextAreaImpl;

public class CreateProcedureAction extends BaseDatabaseRefactorAction {
    public CreateProcedureAction() {
        super("Create Procedure");
    }

    public RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return new RefactorWizard("Create Procedure", new SingleValueWizardPageTextAreaImpl("Procedure Definition"));
    }

    public Change[] createChanges(DatabaseObject selectedTable, RefactorWizardPage... pages) {
        CreateProcedureChange change = new CreateProcedureChange();
        change.setProcedureBody(((SingleValueWizardPageImpl) pages[0]).getValue());

        return new Change[] { change };
    }
}
