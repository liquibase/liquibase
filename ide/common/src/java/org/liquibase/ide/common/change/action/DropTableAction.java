package org.liquibase.ide.common.change.action;

import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.DropTableChange;
import liquibase.database.structure.Table;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddColumnWizardPage;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class DropTableAction  extends BaseTableRefactorAction {

    public DropTableAction() {
        super("Drop Table");
    }

    @Override
    public RefactorWizard createRefactorWizard(Table dbObject) {
        return new RefactorWizard("Drop table "+ dbObject);
    }

    @Override
    protected Change[] createChanges(Table selectedTable, RefactorWizardPage... pages) {
        DropTableChange change = new DropTableChange();
        change.setTableName(selectedTable.getName());

        return new Change[]{change};
    }
}
