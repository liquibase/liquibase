package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.openapi.project.Project;
import dbhelp.db.Column;
import liquibase.change.AddAutoIncrementChange;
import liquibase.change.Change;

import java.sql.Connection;

import org.liquibase.intellij.plugin.change.wizard.page.BaseRefactorWizardPage;

public class AddAutoIncrementWizard extends IntellijRefactorWizard {
    private Column selectedColumn;

    public AddAutoIncrementWizard(Project project, Column column, Connection connection, Object selectedObject) {
//        super("Add Auto-Increment", project, column.getTable().getDatabase(), connection, selectedObject);
        super(null, null, null, null);
        this.selectedColumn = column;
    }


    protected BaseRefactorWizardPage[] createPages() {
        return new BaseRefactorWizardPage[0];
    }


    protected Change[] createChanges() {
        AddAutoIncrementChange change = new AddAutoIncrementChange();
        Column column = selectedColumn;
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setColumnDataType(column.getTypeString());
        return new Change[]{change};
    }
}
