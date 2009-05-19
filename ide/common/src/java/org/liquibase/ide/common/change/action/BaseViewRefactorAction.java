package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.View;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BaseViewRefactorAction extends BaseRefactorAction {

    public BaseViewRefactorAction(String title) {
        super(title);
    }

    public abstract RefactorWizard createRefactorWizard(View dbObject);

    @Override
    public final RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return createRefactorWizard((View) dbObject);
    }

    @Override
    public Change[] createChanges(DatabaseObject selectedView, RefactorWizardPage... pages) {
        return createChanges(((View) selectedView), pages);
    }

    protected abstract Change[] createChanges(View selectedView, RefactorWizardPage... pages);

    @Override
    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(View.class);
    }
}