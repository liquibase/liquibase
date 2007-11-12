package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public class AddNotNullConstraintWizardPageImpl implements AddNotNullConstraintWizardPage {
    private JTextField textField1;
    private JLabel newDefaultNullValue;
    private JPanel mainPanel;

    public String getDefaultNullValue() {
        return newDefaultNullValue.getText();
    }

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
