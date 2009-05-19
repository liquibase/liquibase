package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.ForeignKey;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public abstract class BaseForeignKeyRefactorAction extends BaseRefactorAction {

    public BaseForeignKeyRefactorAction(String title) {
        super(title);
    }

    public abstract RefactorWizard createRefactorWizard(ForeignKey selectedFK);

    @Override
    public final RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return createRefactorWizard((ForeignKey) dbObject);
    }

    @Override
    public Change[] createChanges(DatabaseObject selectedFK, RefactorWizardPage... pages) {
        return createChanges(((ForeignKey) selectedFK), pages);
    }

    protected abstract Change[] createChanges(ForeignKey selectedFK, RefactorWizardPage... pages);

    @Override
    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(ForeignKey.class);
    }
}