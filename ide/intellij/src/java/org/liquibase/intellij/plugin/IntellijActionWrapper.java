package org.liquibase.intellij.plugin;

import dbhelp.db.Catalog;
import dbhelp.db.Schema;
import dbhelp.db.model.AbstractDBObject;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.PortableAction;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import org.liquibase.ide.common.action.BaseDatabaseAction;
import org.liquibase.ide.common.change.action.BaseRefactorAction;
import org.liquibase.intellij.plugin.change.wizard.IntellijRefactorWizard;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.text.ParseException;

public class IntellijActionWrapper extends PortableAction {
    private BaseDatabaseAction action;


    public IntellijActionWrapper(BaseDatabaseAction action) {
        super(action.getTitle());
        this.action = action;
    }

    public void actionPerformed(ActionEvent actionEvent) {
        if (action instanceof BaseRefactorAction) {
            IntellijRefactorWizard wizard = new IntellijRefactorWizard(((BaseRefactorAction) action).createRefactorWizard(getSelectedDatabaseObject()), getSelectedDatabaseObject(), getSelectedDatabase(), ((BaseRefactorAction) action));
            wizard.pack();
            wizard.show();
        } else {
//            action.actionPerform();
        }
    }

    public DatabaseObject getSelectedDatabaseObject() {
        try {
            Object selectedObject = getUserData();
            if (selectedObject instanceof DBTree) {
                TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

                selectedObject = selectionPath.getPathComponent(1);
            }

            if (selectedObject instanceof Catalog) {
                return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(((Catalog) selectedObject).getDatabase().getConnection());
            } else if (selectedObject instanceof dbhelp.db.Column) {
                dbhelp.db.Column selectedColumn = (dbhelp.db.Column) selectedObject;
                dbhelp.db.Table selectedTable = selectedColumn.getTable();

                Table table = createTableObject(selectedTable);

                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(selectedTable.getDatabase().getConnection());

                return createColumnObject(selectedColumn, database, table);
            } else if (selectedObject instanceof Schema) {
                return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(((Schema) selectedObject).getDatabase().getConnection());
            } else if (selectedObject instanceof dbhelp.db.Table) {
                dbhelp.db.Table selectedTable = (dbhelp.db.Table) selectedObject;

                return createTableObject(selectedTable);
            } else if (selectedObject instanceof AbstractDBObject.ProfileNode) {
                return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(((AbstractDBObject.ProfileNode) selectedObject).getDatabase().getConnection());
            } else {
                throw new RuntimeException("Unknown object type: " + selectedObject.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Column createColumnObject(dbhelp.db.Column selectedColumn, Database database, Table table) throws ParseException {
        Column column = new Column();
        column.setName(selectedColumn.getName());
        column.setAutoIncrement(selectedColumn.isAutoIncrement());
        column.setColumnSize(selectedColumn.getColumnSize());
        column.setDataType(selectedColumn.getType());
        column.setDecimalDigits(selectedColumn.getDecimalDigits());
        column.setDefaultValue(database.convertDatabaseValueToJavaObject(selectedColumn.getColumnDef(), selectedColumn.getType(), selectedColumn.getColumnSize(), selectedColumn.getDecimalDigits()));
        column.setNullable(selectedColumn.getNullable() == ResultSetMetaData.columnNullable || selectedColumn.getNullable() == ResultSetMetaData.columnNullableUnknown);
        column.setPrimaryKey(selectedColumn.isPrimaryKey());
        column.setTypeName(selectedColumn.getTypeName());
        column.setTable(table);
        return column;
    }

    private Table createTableObject(dbhelp.db.Table selectedTable) {
        Table table = new Table(selectedTable.getName());
        return table;
    }


    public Database getSelectedDatabase() {
        Object selectedObject = getUserData();
        if (selectedObject instanceof DBTree) {
            TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

            selectedObject = selectionPath.getPathComponent(1);
        }

        try {
            Connection connection;
            if (selectedObject instanceof Catalog) {
                connection = ((Catalog) selectedObject).getDatabase().getConnection();
            } else if (selectedObject instanceof dbhelp.db.Column) {
                connection = ((dbhelp.db.Column) selectedObject).getTable().getDatabase().getConnection();
            } else if (selectedObject instanceof Schema) {
                connection = ((Schema) selectedObject).getDatabase().getConnection();
            } else if (selectedObject instanceof dbhelp.db.Table) {
                connection = ((dbhelp.db.Table) selectedObject).getDatabase().getConnection();
            } else if (selectedObject instanceof AbstractDBObject.ProfileNode) {
                connection = ((AbstractDBObject.ProfileNode) selectedObject).getDatabase().getConnection();
            } else {
                throw new RuntimeException("Unknown object type: " + selectedObject.getClass().getName());
            }
            return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Column getSelectedColumn() {
        Object selectedObject = getUserData();
        if (selectedObject instanceof DBTree) {
            TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

            selectedObject = selectionPath.getPathComponent(1);
        }

        if (selectedObject instanceof Column) {
            return (Column) selectedObject;
        }

        return null;
    }

    public Table getSelectedTable() {
        Object selectedObject = getUserData();
        if (selectedObject instanceof DBTree) {
            TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

            selectedObject = selectionPath.getLastPathComponent();
        }

        if (selectedObject instanceof AbstractDBObject.TableNode) {
            return createTableObject(((AbstractDBObject.TableNode) selectedObject).getTable());
        } else if (selectedObject instanceof Column) {
            return ((Column) selectedObject).getTable();
        } else if (selectedObject instanceof Table) {
            return ((Table) selectedObject);
        }

        return null;
    }

    public DatabaseConnection getSelectedConnection() {
        try {
            return getSelectedDatabase().getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getSelectedObject() {
        Object userData = getUserData();
        if (userData instanceof JTree) {
            TreePath selectionPath = ((DBTree) userData).getSelectionModel().getLeadSelectionPath();
            return selectionPath.getLastPathComponent();
        }

        return userData;
    }
}
