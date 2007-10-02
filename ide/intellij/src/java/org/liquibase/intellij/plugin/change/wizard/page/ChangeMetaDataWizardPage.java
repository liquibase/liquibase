package org.liquibase.intellij.plugin.change.wizard.page;

import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;
import java.util.Date;

import liquibase.util.StringUtils;

public class ChangeMetaDataWizardPage implements Step {
    private JTextArea commentsTextArea;
    private JTextField idTextField;
    private JTextField authorTextField;
    private JCheckBox runOnChangeCheckBox;
    private JCheckBox alwaysRunCheckBox;
    private JTextField contextsTextField;
    private JTextField dbmsTextField;
    private JPanel mainPanel;


    public void _init() {
        ;
    }

    public void _commit(boolean b) throws CommitStepException {
        ;
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
}
