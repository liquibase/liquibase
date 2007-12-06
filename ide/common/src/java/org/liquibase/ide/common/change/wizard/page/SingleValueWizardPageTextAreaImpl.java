package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public class SingleValueWizardPageTextAreaImpl implements SingleValueWizardPage {
    private JTextArea value;
    private JLabel label;
    private JPanel mainPanel;

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public SingleValueWizardPageTextAreaImpl(String label) {
        this(label, null);
    }

    public SingleValueWizardPageTextAreaImpl(String label, String defaultValue) {
        this.label.setText(label);
        this.value.setText(defaultValue);
    }

    public String getValue() {
        return value.getText();
    }
}
