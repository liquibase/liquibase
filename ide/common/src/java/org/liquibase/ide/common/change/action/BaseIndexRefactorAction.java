package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Index;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BaseIndexRefactorAction extends BaseRefactorAction {

    public BaseIndexRefactorAction(String title) {
        super(title);
    }

    public abstract RefactorWizard createRefactorWizard(Index selectedIndex);

    public final RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return createRefactorWizard((Index) dbObject);
    }

    public Change[] createChanges(DatabaseObject selectedFK, RefactorWizardPage... pages) {
        return createChanges(((Index) selectedFK), pages);
    }

    protected abstract Change[] createChanges(Index selectedFK, RefactorWizardPage... pages);

    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(Index.class);
    }
}