package org.liquibase.intellij.plugin;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import liquibase.resource.CompositeFileOpener;
import liquibase.resource.FileSystemFileOpener;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.util.ISODateFormat;
import liquibase.util.xml.DefaultXmlWriter;
import liquibase.util.xml.XmlWriter;
import org.liquibase.ide.common.ChangeLogWriter;
import org.liquibase.ide.common.IdeFacade;
import org.liquibase.ide.common.ProgressMonitor;
import org.liquibase.intellij.plugin.dialog.DisplayOutputDialogImpl;
import org.liquibase.intellij.plugin.dialog.SelectChangeLogDialogImpl;

import java.io.File;
import java.text.ParseException;
import java.util.Date;

public class IntellijFacade implements IdeFacade {

    public ProgressMonitor getProgressMonitor() {
        return new IntellijProgressMonitor();
    }


    public Liquibase getLiquibase(String changeLogFile, Database database) {
        LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();

        if (changeLogFile == null) {
            changeLogFile = liquibaseProjectComponent.getRootChangeLogFile();
        }

        Liquibase liquibase = new Liquibase(changeLogFile, new CompositeFileOpener(new IntellijFileOpener(), new FileSystemFileOpener()), database);
        try {
            liquibase.checkDatabaseChangeLogTable();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return liquibase;
    }

    public XmlWriter getXmlWriter() {
        return new DefaultXmlWriter();
    }

    public DatabaseChangeLog getRootChangeLog() {
//        XmlFile xmlFile = LiquibaseProjectComponent.getInstance().getChangeLogFile();
//        return new DatabaseChangeLog(getMigrator())
        return new DatabaseChangeLog(LiquibaseProjectComponent.getInstance().getRootChangeLogFile());
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
        if (files == null || files.length == 0 || files[0] == null) {
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

    public void showMessage(String title, String message) {
        Messages.showInfoMessage(message, title);
    }

    public void showOutput(String title, String output) {
        DisplayOutputDialogImpl.showOutputDialog(title, output);
    }

    public boolean confirm(String title, String message) {
        int result = Messages.showYesNoDialog(LiquibaseProjectComponent.getInstance().getProject(), message, title, Messages.getQuestionIcon());

        return result == 0;
    }

    public String promptForChangeLogFile() {
        LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();
        if (liquibaseProjectComponent.getPromptForChangeLog()) {
            return new SelectChangeLogDialogImpl().selectChangeLogFile();
        } else {
            return liquibaseProjectComponent.getRootChangeLogFile(); 
        }
    }
}
