package org.liquibase.ide.common;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.migrator.Migrator;

import java.util.Date;
import java.io.File;

public interface IdeFacade {
    ProgressMonitor getProgressMonitor();

    Migrator getMigrator(Database database);

    DatabaseChangeLog getRootChangeLog();

    ChangeLogWriter getChangeLogWriter();

    String promptForString(String title, String message, String defaultValue);

    Integer promptForInteger(String title, String message, Integer defaultValue);

    Date promptForDateTime(String title, String message, Date defaultValue);

    void displayOutput(String title, String output);

    void displayMessage(String title, String message);

    void showError(String title, Exception exception);

    void showError(Exception exception);

    File promptForDirectory(String title, String message, File defaultFile);

    boolean confirm(String title, String message);
}
