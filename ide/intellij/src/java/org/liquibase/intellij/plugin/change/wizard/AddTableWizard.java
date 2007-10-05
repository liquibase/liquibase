package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import dbhelp.db.Database;
import dbhelp.db.IDBObject;
import dbhelp.db.INamedDBObject;
import dbhelp.db.model.AbstractDBObject;
import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.ConstraintsConfig;
import liquibase.change.CreateTableChange;
import liquibase.database.structure.Column;
import org.liquibase.intellij.plugin.change.wizard.page.AddTableWizardPage;

import java.sql.Connection;

public class AddTableWizard extends BaseIntellijRefactorWizard {
    private AddTableWizardPage wizardPage;
    private AbstractDBObject selectedObject;

    public AddTableWizard(Project project, Database database, Connection connection, AbstractDBObject selectedObject) {
        super("Create Table", project, database, connection);
        this.selectedObject = selectedObject;
    }


    protected Step[] createPages() {
        wizardPage = new AddTableWizardPage();
        return new Step[]{
                wizardPage,
        };
    }


    protected Change[] createChanges() {
        CreateTableChange change = new CreateTableChange();
        change.setTableName(wizardPage.getTableName());
        for (ColumnConfig columnConfig : wizardPage.getColumns()) {
            change.addColumn(columnConfig);
        }

        return new Change[]{change};
    }

    protected void refresh() {
        selectedObject.refresh();
    }
}
