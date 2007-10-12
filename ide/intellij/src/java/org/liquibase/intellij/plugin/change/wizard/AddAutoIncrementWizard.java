package org.liquibase.intellij.plugin.change.wizard;

import com.intellij.ide.wizard.Step;
import com.intellij.openapi.project.Project;
import dbhelp.db.Column;
import dbhelp.db.Database;
import dbhelp.db.IDBObject;
import dbhelp.db.model.AbstractDBObject;
import liquibase.change.AddAutoIncrementChange;
import liquibase.change.Change;

import java.sql.Connection;

public class AddAutoIncrementWizard extends BaseIntellijRefactorWizard {
    private Column selectedColumn;

    public AddAutoIncrementWizard(Project project, Column column, Connection connection, Object selectedObject) {
        super("Add Auto-Increment", project, column.getTable().getDatabase(), connection, selectedObject);
        this.selectedColumn = column;
    }


    protected Step[] createPages() {
        return new Step[0];
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
