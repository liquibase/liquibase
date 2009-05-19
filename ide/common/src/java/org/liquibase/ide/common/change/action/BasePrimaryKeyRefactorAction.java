package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.PrimaryKey;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BasePrimaryKeyRefactorAction extends BaseRefactorAction {

    public BasePrimaryKeyRefactorAction(String title) {
        super(title);
    }

    public abstract RefactorWizard createRefactorWizard(PrimaryKey selectedIndex);

    @Override
    public final RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return createRefactorWizard((PrimaryKey) dbObject);
    }

    @Override
    public Change[] createChanges(DatabaseObject selectedPK, RefactorWizardPage... pages) {
        return createChanges(((PrimaryKey) selectedPK), pages);
    }

    protected abstract Change[] createChanges(PrimaryKey selectedPK, RefactorWizardPage... pages);

    @Override
    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(PrimaryKey.class);
    }
}