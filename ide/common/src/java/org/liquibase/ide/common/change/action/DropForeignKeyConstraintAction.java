package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.database.structure.ForeignKey;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropForeignKeyConstraintAction extends BaseForeignKeyRefactorAction {

    public DropForeignKeyConstraintAction() {
        super("Drop Foreign Key");
    }

    @Override
    public RefactorWizard createRefactorWizard(ForeignKey selectedFK) {
        return new RefactorWizard("Drop foreign key "+selectedFK);
    }

    @Override
    protected Change[] createChanges(ForeignKey selectedFK, RefactorWizardPage... pages) {
        DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
        change.setConstraintName(selectedFK.getName());
        change.setBaseTableName(selectedFK.getPrimaryKeyTable().getName());

        return new Change[] { change };
    }
}
