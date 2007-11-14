package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.RenameColumnChange;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class RenameColumnAction extends BaseColumnRefactorAction {
    public RenameColumnAction() {
        super("Rename Column");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Rename "+selectedColumn, new SingleValueWizardPageImpl("New Column Name", selectedColumn.getName()));
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        RenameColumnChange change = new RenameColumnChange();
        change.setTableName(column.getTable().getName());
        change.setOldColumnName(column.getName());
        change.setNewColumnName(((SingleValueWizardPage) pages[0]).getValue());
        change.setColumnDataType(column.getDataTypeString(column.getTable().getDatabase()));

        return new Change[] { change };
    }
}
