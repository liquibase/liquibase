package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.structure.DatabaseObject;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddTableWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddTableWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddTableAction extends BaseDatabaseRefactorAction {


    public AddTableAction() {
        super("Add Table");
    }


    public RefactorWizard createRefactorWizard(DatabaseObject dbObject) {
        return new RefactorWizard("Create New Table", new AddTableWizardPageImpl());
    }

    public Change[] createChanges(DatabaseObject selectedTable, RefactorWizardPage... pages) {
        AddTableWizardPage wizardPage = (AddTableWizardPage) pages[0];
        CreateTableChange change = new CreateTableChange();
        change.setTableName(wizardPage.getTableName());
        for (ColumnConfig columnConfig : wizardPage.getColumns()) {
            change.addColumn(columnConfig);
        }

        return new Change[]{change};
    }
}
