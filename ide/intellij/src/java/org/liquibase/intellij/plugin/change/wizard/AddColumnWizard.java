package org.liquibase.intellij.plugin.change.wizard;

import dbhelp.db.Table;
import org.liquibase.intellij.plugin.change.wizard.page.AddColumnWizardPage;
import com.intellij.openapi.project.Project;
import com.intellij.ide.wizard.Step;

import java.sql.Connection;

import liquibase.change.Change;
import liquibase.change.AddColumnChange;
import liquibase.change.ColumnConfig;

public class AddColumnWizard extends BaseIntellijRefactorWizard {
    private Table selectedTable;
    private AddColumnWizardPage wizardPage;


    public AddColumnWizard(Project project, Table table, Connection connection, Object selectedObject) {
        super("Add Column to "+table.getName(), project, table.getDatabase(), connection, selectedObject);
        this.selectedTable = table;
    }


    protected Step[] createPages() {
        wizardPage = new AddColumnWizardPage();
        return new Step[]{
                wizardPage,
        };
    }


    protected Change[] createChanges() {
        AddColumnChange change = new AddColumnChange();
        change.setTableName(selectedTable.getName());

        ColumnConfig config = new ColumnConfig();
        config.setName(wizardPage.getColumnName());
        config.setType(wizardPage.getColumnType());

        change.setColumn(config);
        return new Change[]{change};
    }
}
