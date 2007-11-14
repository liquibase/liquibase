package org.liquibase.intellij.plugin;

import dbhelp.db.Catalog;
import dbhelp.db.Schema;
import dbhelp.db.model.AbstractDBObject;
import dbhelp.db.model.DBTreeModel;
import dbhelp.db.model.IDBNode;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.PortableAction;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.structure.Column;
import liquibase.database.structure.DatabaseObject;
import liquibase.database.structure.Table;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.action.BaseDatabaseAction;
import org.liquibase.ide.common.action.MigratorAction;
import org.liquibase.ide.common.change.action.BaseRefactorAction;
import org.liquibase.intellij.plugin.change.wizard.IntellijRefactorWizard;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSetMetaData;
import java.text.ParseException;
import java.util.Enumeration;

public class IntellijActionWrapper extends PortableAction {
    private BaseDatabaseAction action;
    private IdeFacade ideFacade;


    public IntellijActionWrapper(BaseDatabaseAction action, Class objectType) {
        super(action.getTitle());
        this.action = action;
        setEnabled(action.isApplicableTo(objectType));
        ideFacade = LiquibaseProjectComponent.getInstance().getIdeFacade();
    }

    public void actionPerformed(ActionEvent actionEvent) {
        try {
            DatabaseObject selectedDatabaseObject = getSelectedDatabaseObject();
            Database selectedDatabase = getSelectedDatabase();
            if (action instanceof BaseRefactorAction) {
                IntellijRefactorWizard wizard = new IntellijRefactorWizard(((BaseRefactorAction) action).createRefactorWizard(selectedDatabaseObject), selectedDatabaseObject, selectedDatabase, ((BaseRefactorAction) action));
                wizard.pack();
                wizard.show();
            } else if (action instanceof MigratorAction) {
                ((MigratorAction) action).actionPerform(selectedDatabase, ideFacade);
            }

            if (action.needsRefresh()) {
                refresh();
            }
        } catch (LiquibaseException e) {
            ideFacade.showError("Error Executing Change", e);
        }
    }

    private void refresh() {
        LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();
        DBTree tree = liquibaseProjectComponent.getDbTree();
        if (tree == null) {
            liquibaseProjectComponent.getIdeFacade().showMessage("Operation Complete", "Could not auto-refresh the database display\n\nPlease refresh it manually");
            return;
        }

        TreePath pathToRefresh = tree.getSelectionPath().getParentPath();
        Enumeration<TreePath> expandedPaths = tree.getExpandedDescendants(new TreePath(tree.getModel().getRoot()));
        Object c = pathToRefresh.getLastPathComponent();
        if (c instanceof IDBNode) {
            IDBNode dbObject = (IDBNode) c;
            dbObject.refresh();
            DBTreeModel m = (DBTreeModel) tree.getModel();
            m.fireTreeNodeChange(new TreeModelEvent(this, pathToRefresh));
            while (!(dbObject instanceof AbstractDBObject.ProfileNode) && null != dbObject) {
                dbObject = dbObject.getParent();
            }
            if (dbObject != null) {
                AbstractDBObject.ProfileNode dbNode = (AbstractDBObject.ProfileNode) dbObject;
                try {
                    dbNode.getDatabase().getResolver().refresh();
                } catch (Exception e1) {
                    liquibaseProjectComponent.getIdeFacade().showError(e1);
                }
            }
        }

        while (expandedPaths != null && expandedPaths.hasMoreElements()) {
            TreePath path = expandedPaths.nextElement();
            tree.expandPath(path);
            TreeExpansionListener[] expansionListeners = tree.getListeners(TreeExpansionListener.class);
            for (TreeExpansionListener listener : expansionListeners) {
                listener.treeExpanded(new TreeExpansionEvent(this, path));
            }
        }

    }

    public DatabaseObject getSelectedDatabaseObject() {
        try {
            Object selectedObject = getUserData();
            if (selectedObject instanceof DBTree) {
                TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

                selectedObject = selectionPath.getPathComponent(1);
            }

            if (selectedObject instanceof dbhelp.db.Database) {
                return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(((dbhelp.db.Database) selectedObject).getConnection());
            } else if (selectedObject instanceof Catalog) {
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

    private Column createColumnObject
            (dbhelp.db.Column selectedColumn,
             Database database,
             Table table) throws ParseException {
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
        table.setDatabase(getSelectedDatabase());
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
            if (selectedObject instanceof dbhelp.db.Database) {
                connection = ((dbhelp.db.Database) selectedObject).getConnection();
            } else if (selectedObject instanceof Catalog) {
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
        } catch (Exception
                e) {
            throw new RuntimeException(e);
        }
    }

    public Column getSelectedColumn
            () {
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

    public Table getSelectedTable
            () {
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

    public DatabaseConnection getSelectedConnection
            () {
        try {
            return getSelectedDatabase().getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Object getSelectedObject
            () {
        Object userData = getUserData();
        if (userData instanceof JTree) {
            TreePath selectionPath = ((DBTree) userData).getSelectionModel().getLeadSelectionPath();
            return selectionPath.getLastPathComponent();
        }

        return userData;
    }
}
