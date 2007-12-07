package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.DropIndexChange;
import liquibase.database.structure.Index;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropIndexAction extends BaseIndexRefactorAction {

    public DropIndexAction() {
        super("Drop Index");
    }

    public RefactorWizard createRefactorWizard(Index selectedIndex) {
        return new RefactorWizard("Drop index "+selectedIndex);
    }

    protected Change[] createChanges(Index selectedIndex, RefactorWizardPage... pages) {
        DropIndexChange change = new DropIndexChange();
        change.setIndexName(selectedIndex.getName());
        change.setTableName(selectedIndex.getTable().getName());

        return new Change[] { change };
    }
}
