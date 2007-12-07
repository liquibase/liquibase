package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.DropPrimaryKeyChange;
import liquibase.database.structure.PrimaryKey;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropPrimaryKeyAction extends BasePrimaryKeyRefactorAction {

    public DropPrimaryKeyAction() {
        super("Drop Primary Key");
    }

    public RefactorWizard createRefactorWizard(PrimaryKey selectedPK) {
        return new RefactorWizard("Drop primary key "+selectedPK);
    }

    protected Change[] createChanges(PrimaryKey selectedPK, RefactorWizardPage... pages) {
        DropPrimaryKeyChange change = new DropPrimaryKeyChange();
        change.setConstraintName(selectedPK.getName());
        change.setTableName(selectedPK.getTable().getName());

        return new Change[] { change };
    }
}