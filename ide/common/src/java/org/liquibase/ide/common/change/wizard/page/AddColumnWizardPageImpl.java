package org.liquibase.ide.common.change.wizard.page;


import javax.swing.*;


public class AddColumnWizardPageImpl implements AddColumnWizardPage {
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


    public void init() {

    }
}
