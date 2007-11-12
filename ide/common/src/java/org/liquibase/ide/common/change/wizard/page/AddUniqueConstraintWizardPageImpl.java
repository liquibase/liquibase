package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public class AddUniqueConstraintWizardPageImpl implements AddUniqueConstraintWizardPage {
    private JTextField constraintName;
    private JPanel mainPanel;

    public String getConstraintName() {
        return constraintName.getText();
    }

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }
}
