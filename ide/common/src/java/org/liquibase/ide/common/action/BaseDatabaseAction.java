package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.database.structure.*;

import java.sql.Connection;

import org.liquibase.ide.common.change.wizard.RefactorWizard;

public abstract class BaseDatabaseAction {
    private String title;

    public BaseDatabaseAction(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

//    Database getSelectedDatabase();
//
//    Connection getSelectedConnection();
//
//    Schema getSelectedSchema();
//
//    Table getSelectedTable();
//
//    Column getSelectedColumn();
//
//    ForeignKey getSelectedForeignKey();
//
//    Index getSelectedIndex();
//
//    View getSelectedView();

    public abstract boolean isApplicableTo(Class objectType);
}
