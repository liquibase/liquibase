package org.liquibase.intellij.plugin.action;

import dbhelp.db.*;
import dbhelp.db.model.AbstractDBObject;
import dbhelp.db.ui.DBTree;
import dbhelp.plugin.action.portable.PortableAction;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.sql.Connection;

public abstract class BaseDatabaseAction extends PortableAction {

    public BaseDatabaseAction(String name) {
        super(name);
    }


}
