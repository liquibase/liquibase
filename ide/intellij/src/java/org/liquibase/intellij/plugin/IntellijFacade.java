package org.liquibase.intellij.plugin;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import liquibase.CompositeFileOpener;
import liquibase.DatabaseChangeLog;
import liquibase.FileSystemFileOpener;
import liquibase.database.Database;
import liquibase.migrator.Migrator;
import liquibase.util.ISODateFormat;
import liquibase.xml.DefaultXmlWriter;
import liquibase.xml.XmlWriter;
import org.liquibase.ide.common.ChangeLogWriter;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.ProgressMonitor;
import org.liquibase.intellij.plugin.dialog.SelectChangeLogDialog;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

public class IntellijFacade implements IdeFacade {

    public ProgressMonitor getProgressMonitor() {
        return new IntellijProgressMonitor();
    }


    public Migrator getMigrator(Database database) {
        LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();

        String rootChangeLog = liquibaseProjectComponent.getRootChangeLog();

        if (rootChangeLog == null) {
            String changeLogFile = new SelectChangeLogDialog().selectChangeLogFile();
            if (changeLogFile == null) {
                return null;
            }
            liquibaseProjectComponent.setRootChangeLog(changeLogFile);


        }

        Migrator migrator = new Migrator(rootChangeLog, new CompositeFileOpener(new IntellijFileOpener(), new FileSystemFileOpener()));
        if (database == null) {
            return migrator;
        }
        try {
            migrator.init(database);
            migrator.checkDatabaseChangeLogTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return migrator;
    }

    public XmlWriter getXmlWriter() {
        return new DefaultXmlWriter();
    }

    public DatabaseChangeLog getRootChangeLog() {
//        XmlFile xmlFile = LiquibaseProjectComponent.getInstance().getChangeLogFile();
//        return new DatabaseChangeLog(getMigrator())
        return new DatabaseChangeLog(getMigrator(null), LiquibaseProjectComponent.getInstance().getRootChangeLog());
    }


    public ChangeLogWriter getChangeLogWriter() {
        return new IntellijChangeLogWriter();
    }

    public String promptForString(String title, String message, String defaultValue) {
        return Messages.showInputDialog(LiquibaseProjectComponent.getInstance().getProject(),
                message,
                title,
                Messages.getQuestionIcon(),
                defaultValue,
                new InputValidator() {
                    public boolean checkInput(String s) {
                        return true;
                    }

                    public boolean canClose(String s) {
                        return true;
                    }
                });
    }

    public File promptForDirectory(String title, String message, File defaultFile) {
        FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor();
        descriptor.setTitle(title);
        descriptor.setDescription(title);
        Project project = LiquibaseProjectComponent.getInstance().getProject();
        FileChooserDialog fileChooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
        VirtualFile[] files = fileChooserDialog.choose(null, project);
        if (files == null) {
            return null;
        }
        return new File(files[0].getUrl());
    }

    public Integer promptForInteger(String title, String message, Integer defaultValue) {
        String input = Messages.showInputDialog(
                LiquibaseProjectComponent.getInstance().getProject(),
                message,
                title,
                Messages.getQuestionIcon(),
                defaultValue.toString(),
                new InputValidator() {
                    public boolean checkInput(String s) {
                        return s.matches("\\d+");
                    }

                    public boolean canClose(String s) {
                        return true;
                    }
                });
        if (input == null) {
            return null;
        }
        return Integer.parseInt(input);
    }

    public Date promptForDateTime(String title, String message, Date defaultValue) {
        final ISODateFormat dateFormat = new ISODateFormat();

        String input = Messages.showInputDialog(
                LiquibaseProjectComponent.getInstance().getProject(),
                message,
                title,
                Messages.getQuestionIcon(),
                defaultValue.toString(),
                new InputValidator() {
                    public boolean checkInput(String s) {
                        try {
                            dateFormat.parse(s);
                            return true;
                        } catch (ParseException e) {
                            return false;
                        }
                    }

                    public boolean canClose(String s) {
                        return true;
                    }
                });
        if (input == null) {
            return null;
        }
        try {
            return dateFormat.parse(input);
        } catch (ParseException e) {
            showError(e);
            return null;
        }
    }

    public void showError(String title, Exception exception) {
        Messages.showErrorDialog(LiquibaseProjectComponent.getInstance().getProject(), exception.getMessage(), title);
        exception.printStackTrace();
    }

    public void showError(Exception exception) {
        showError("Unexpected Error", exception);
    }

    public void displayMessage(String title, String message) {
        Messages.showInfoMessage(message, title);
    }

    public void displayOutput(String title, String output) {
        Messages.showInfoMessage(output, title);
    }

    public boolean confirm(String title, String message) {
        int result = Messages.showYesNoDialog(LiquibaseProjectComponent.getInstance().getProject(), message, title, Messages.getQuestionIcon());

        return result == 0;
    }
}
