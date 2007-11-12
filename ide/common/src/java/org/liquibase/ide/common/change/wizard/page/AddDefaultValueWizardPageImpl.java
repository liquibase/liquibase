package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public class AddDefaultValueWizardPageImpl implements AddDefaultValueWizardPage {
    private JTextField defaultValue;
    private JPanel mainPanel;

    public String getDefaultValue() {
        return defaultValue.getText();
    }

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
