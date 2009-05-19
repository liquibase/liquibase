package org.liquibase.ide.common.change.action;

import liquibase.change.AddColumnChange;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.database.structure.Table;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddColumnWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddColumnWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddColumnAction extends BaseTableRefactorAction {

    public AddColumnAction() {
        super("Add Column");
    }

    @Override
    public RefactorWizard createRefactorWizard(Table dbObject) {
        return new RefactorWizard("Add column to "+ ((Table) dbObject).getName(), new AddColumnWizardPageImpl());
    }

    @Override
    protected Change[] createChanges(Table selectedTable, RefactorWizardPage... pages) {
        AddColumnChange change = new AddColumnChange();
        change.setTableName(selectedTable.getName());

        AddColumnWizardPage wizardPage = (AddColumnWizardPage) pages[0];

        ColumnConfig config = new ColumnConfig();
        config.setName(wizardPage.getColumnName());
        config.setType(wizardPage.getColumnType());

        change.addColumn(config);
        return new Change[]{change};
    }
}
