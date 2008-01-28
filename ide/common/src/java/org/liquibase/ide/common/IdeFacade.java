package org.liquibase.ide.common;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.Liquibase;

import java.util.Date;
import java.io.File;

public interface IdeFacade {
    ProgressMonitor getProgressMonitor();

    Liquibase getLiquibase(String changeLogFile, Database database);

    DatabaseChangeLog getRootChangeLog();

    ChangeLogWriter getChangeLogWriter();

    String promptForString(String title, String message, String defaultValue);

    Integer promptForInteger(String title, String message, Integer defaultValue);

    String promptForChangeLogFile();

    Date promptForDateTime(String title, String message, Date defaultValue);

    void showOutput(String title, String output);

    void showMessage(String title, String message);

    void showError(String title, Exception exception);

    void showError(Exception exception);

    File promptForDirectory(String title, String message, File defaultFile);


    boolean confirm(String title, String message);
}
