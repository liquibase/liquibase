package org.liquibase.ide.common.change.wizard.page;

import org.liquibase.ide.common.AbstractWizardPageWithRequiredFields;

import javax.swing.*;

public class AddLookupTableWizardPageImpl extends AbstractWizardPageWithRequiredFields implements AddLookupTableWizardPage{
    private JTextField newTableName;
    private JTextField newColumnName;
    private JPanel mainPanel;
    private JTextField constraintName;

    public String getConstraintName() {
        return constraintName.getText();
    }

    public String getNewTableName() {
        return newTableName.getText();
    }

    public String getNewColumnName() {
        return newColumnName.getText();
    }

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public JComponent[] getValidationComponents() {
        return new JComponent[] {
                newTableName,
                newColumnName,
                constraintName

        };
    }
}
