package org.liquibase.ide.common.change.wizard.page;


import liquibase.util.StringUtils;
import org.liquibase.ide.common.AbstractWizardPageWithRequiredFields;

import javax.swing.*;


public class AddColumnWizardPageImpl extends AbstractWizardPageWithRequiredFields implements AddColumnWizardPage {
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

    public JComponent[] getValidationComponents() {
        return new JComponent[] {
                columnNameTextField,
                dataTypeTextField,
        };
    }
}
