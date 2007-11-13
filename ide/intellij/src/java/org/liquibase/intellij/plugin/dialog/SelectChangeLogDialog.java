package org.liquibase.intellij.plugin.dialog;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.*;

public class SelectChangeLogDialog {
    private TextFieldWithBrowseButton changeLogSelect;
    private JPanel mainPanel;

    public String selectChangeLogFile() {
        DialogBuilder builder = new DialogBuilder(LiquibaseProjectComponent.getInstance().getProject());
        builder.addOkAction();
        builder.addCancelAction();
        builder.setTitle("Select Change Log");

        builder.setCenterPanel(mainPanel);

        builder.getDialogWrapper().pack();
        builder.showModal(true);
        int i = builder.show();
        if (i == 0) {
            return changeLogSelect.getText();
        } else {
            return null;
        }
    }

    private void createUIComponents() {
        final LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();
        Project project = liquibaseProjectComponent.getProject();

        changeLogSelect = new TextFieldWithBrowseButton();
        final FileChooserDescriptor fileChooser = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        String currentChangeLogFile = liquibaseProjectComponent.getOutputChangeLogFile();
        if (currentChangeLogFile != null) {
            changeLogSelect.setText(currentChangeLogFile);
        }
        changeLogSelect.setTextFieldPreferredWidth(100);
        changeLogSelect.addBrowseFolderListener("Select Change Log File", null, project, fileChooser);
        changeLogSelect.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updatePreference();
            }

            public void removeUpdate(DocumentEvent e) {
                updatePreference();
            }

            public void changedUpdate(DocumentEvent e) {
                updatePreference();
            }

            private void updatePreference() {
                liquibaseProjectComponent.setOutputChangeLogFile(changeLogSelect.getTextField().getText());
            }
        });
    }
}
