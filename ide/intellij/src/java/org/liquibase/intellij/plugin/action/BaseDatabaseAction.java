package org.liquibase.intellij.plugin.action;

import dbhelp.db.*;
import dbhelp.db.model.AbstractDBObject;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.PortableAction;

import javax.swing.tree.TreePath;
import java.sql.Connection;

public abstract class BaseDatabaseAction extends PortableAction {

    public BaseDatabaseAction(String name) {
        super(name);
    }

    public Database getSelectedDatabase() {
        Object userData = getUserData();
        if (userData instanceof DBTree) {
            TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

            Object selectedObject = selectionPath.getPathComponent(1);

            if (selectedObject instanceof Catalog) {
                return ((Catalog) selectedObject).getDatabase();
            } else if (selectedObject instanceof Column) {
                return ((Column) selectedObject).getTable().getDatabase();
            } else if (selectedObject instanceof Schema) {
                return ((Schema) selectedObject).getDatabase();
            } else if (selectedObject instanceof Table) {
                return ((Table) selectedObject).getDatabase();
            } else if (selectedObject instanceof AbstractDBObject.ProfileNode) {
                return ((AbstractDBObject.ProfileNode) selectedObject).getDatabase();
            } else {
                throw new RuntimeException("Unknown object type: " + selectedObject.getClass().getName());
            }
        } else {
            throw new RuntimeException("Unknown object type: " + userData.getClass().getName());
        }
    }

    public Connection getSelectedConnection() {
        try {
            return getSelectedDatabase().getConnection();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AbstractDBObject getSelectedObject() {
        TreePath selectionPath = ((DBTree) getUserData()).getSelectionModel().getLeadSelectionPath();

        return (AbstractDBObject) selectionPath.getLastPathComponent();

    }
}
