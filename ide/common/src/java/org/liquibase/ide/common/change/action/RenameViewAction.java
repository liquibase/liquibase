package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.core.RenameViewChange;
import liquibase.database.structure.View;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class RenameViewAction extends BaseViewRefactorAction {
    public RenameViewAction() {
        super("Rename View");
    }

    @Override
    public RefactorWizard createRefactorWizard(View selectedView) {
        return new RefactorWizard("Rename "+selectedView, new SingleValueWizardPageImpl("New View Name", selectedView.getName()));
    }

    @Override
    protected Change[] createChanges(View view, RefactorWizardPage... pages) {
        RenameViewChange change = new RenameViewChange();
        change.setOldViewName(view.getName());
        change.setNewViewName(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }
}