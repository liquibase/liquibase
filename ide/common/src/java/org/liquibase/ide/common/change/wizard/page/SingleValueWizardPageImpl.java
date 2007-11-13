package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public class SingleValueWizardPageImpl implements SingleValueWizardPage {
    private JTextField value;
    private JLabel label;
    private JPanel mainPanel;

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public SingleValueWizardPageImpl(String label) {
        this.label.setText(label);
    }

    public String getValue() {
        return value.getText();
    }
}
