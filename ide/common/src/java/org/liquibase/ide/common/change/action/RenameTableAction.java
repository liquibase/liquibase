package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.RenameTableChange;
import liquibase.database.structure.Table;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class RenameTableAction extends BaseTableRefactorAction {
    public RenameTableAction() {
        super("Rename Table");
    }

    public RefactorWizard createRefactorWizard(Table selectedTable) {
        return new RefactorWizard("Rename "+selectedTable, new SingleValueWizardPageImpl("New Table Name"));
    }

    protected Change[] createChanges(Table table, RefactorWizardPage... pages) {
        RenameTableChange change = new RenameTableChange();
        change.setOldTableName(table.getName());
        change.setNewTableName(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }
}