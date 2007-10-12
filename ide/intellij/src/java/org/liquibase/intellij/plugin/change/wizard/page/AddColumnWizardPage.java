package org.liquibase.intellij.plugin.change.wizard.page;

import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;

public class AddColumnWizardPage extends BaseRefactorWizardPage {
    private JPanel mainPanel;
    private JTextField columnNameTextField;
    private JTextField dataTypeTextField;

    public JComponent getComponent() {
        return mainPanel;
    }

    public String getColumnName() {
        return columnNameTextField.getText();
    }

    public String getColumnType() {
        return dataTypeTextField.getText();
    }


}
