package org.liquibase.intellij.plugin.dialog;

import org.liquibase.ide.common.dialog.DisplayOutputDialog;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

import javax.swing.*;

import com.intellij.openapi.ui.DialogBuilder;

public class DisplayOutputDialogImpl implements DisplayOutputDialog {
    private JPanel mainPanel;
    private JTextArea outputArea;

    private DisplayOutputDialogImpl() {
    }

    public static void showOutputDialog(String title, String output) {

        DisplayOutputDialogImpl dialog = new DisplayOutputDialogImpl();

        LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();
        DialogBuilder builder = new DialogBuilder(liquibaseProjectComponent.getProject());
        builder.addCloseButton();
        builder.setTitle(title);

        dialog.outputArea.setText(output);
        builder.setCenterPanel(dialog.mainPanel);

        builder.showNotModal();
    }
}
