package org.liquibase.intellij.plugin.change.wizard.page;

import com.intellij.ide.wizard.CommitStepException;
import com.intellij.ide.wizard.Step;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import liquibase.util.StringUtils;
import org.liquibase.ide.common.AbstractWizardPageWithRequiredFields;
import org.liquibase.intellij.plugin.LiquibaseProjectComponent;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class ChangeMetaDataWizardPage extends AbstractWizardPageWithRequiredFields implements org.liquibase.ide.common.change.wizard.page.ChangeMetaDataWizardPage, Step {
    private JTextArea commentsTextArea;
    private JTextField idTextField;
    private JTextField authorTextField;
    private JCheckBox runOnChangeCheckBox;
    private JCheckBox alwaysRunCheckBox;
    private JTextField contextsTextField;
    private JTextField dbmsTextField;
    private JPanel mainPanel;
    private TextFieldWithBrowseButton changeLogFile;

    private Project project;


    public ChangeMetaDataWizardPage(Project project) {
        this.project = project;
    }


    public void _init() {

    }

    public void _commit(boolean b) throws CommitStepException {

    }

    public Icon getIcon() {
        return null;
    }

    public JComponent getComponent() {
        idTextField.setText(String.valueOf(new Date().getTime()));
        authorTextField.setText(StringUtils.trimToEmpty(System.getProperty("user.name")));

        return mainPanel;
    }

    public String getId() {
        return idTextField.getText();
    }

    public String getAuthor() {
        return authorTextField.getText();
    }

    public boolean isAlwaysRun() {
        return alwaysRunCheckBox.isSelected();
    }

    public boolean isRunOnChange() {
        return runOnChangeCheckBox.isSelected();
    }

    public String getContext() {
        return contextsTextField.getText();
    }

    public String getDbms() {
        return dbmsTextField.getText();
    }

    public String getComments() {
        return commentsTextArea.getText();
    }

    private void createUIComponents() {
        final LiquibaseProjectComponent liquibaseProjectComponent = LiquibaseProjectComponent.getInstance();
        Project project = liquibaseProjectComponent.getProject();

        changeLogFile = new TextFieldWithBrowseButton(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getSource());
            }
        });
        final FileChooserDescriptor fileChooser = FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        String currentChangeLogFile = liquibaseProjectComponent.getOutputChangeLogFile();
        if (currentChangeLogFile != null) {
            changeLogFile.setText(currentChangeLogFile);
        }
        changeLogFile.setTextFieldPreferredWidth(100);
        changeLogFile.addBrowseFolderListener("Select Change Log File", null, project, fileChooser);
        changeLogFile.getTextField().getDocument().addDocumentListener(new DocumentListener() {
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
                liquibaseProjectComponent.setOutputChangeLogFile(changeLogFile.getTextField().getText());
            }
        });
    }

    public JComponent[] getValidationComponents() {
        return new JComponent[] {
                idTextField,
                changeLogFile.getTextField(),
                authorTextField,
        };
    }
}
