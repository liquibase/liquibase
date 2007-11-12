package org.liquibase.ide.common.change.action;

import liquibase.change.AddLookupTableChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddLookupTableWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddLookupTableWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddLookupTableAction  extends BaseColumnRefactorAction {
    public AddLookupTableAction() {
        super("Add Lookup Table");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add lookup table for "+selectedColumn, new AddLookupTableWizardPageImpl());
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddLookupTableChange change = new AddLookupTableChange();
        change.setExistingTableName(column.getTable().getName());
        change.setExistingColumnName(column.getName());

        change.setConstraintName(((AddLookupTableWizardPage) pages[0]).getConstraintName());

        change.setNewTableName(((AddLookupTableWizardPage) pages[0]).getNewTableName());
        change.setNewColumnName(((AddLookupTableWizardPage) pages[0]).getNewColumnName());
        change.setNewColumnDataType(column.getTypeName());

        return new Change[] { change };
    }
}