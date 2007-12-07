package org.liquibase.ide.common.change.wizard.page;

import liquibase.util.StringUtils;
import org.liquibase.ide.common.AbstractWizardPageWithRequiredFields;

import javax.swing.*;

public class AddForeignKeyConstraintWizardPageImpl extends AbstractWizardPageWithRequiredFields implements AddForeignKeyConstraintWizardPage {
    private JTextField constraintName;
    private JTextField referencedTableName;
    private JTextField referencedColumnNames;
    private JCheckBox deleteCascade;
    private JCheckBox deferrable;
    private JCheckBox initiallyDeferred;
    private JPanel mainPanel;

    public String getConstraintName() {
        return constraintName.getText();
    }

    public String getReferencedTableName() {
        return referencedTableName.getText();
    }

    public String getReferencedColumnNames() {
        return referencedColumnNames.getText();
    }

    public boolean getInitiallyDeferred() {
        return initiallyDeferred.isSelected();
    }

    public boolean getDeferrable() {
        return deferrable.isSelected();
    }

    public boolean getDeleteCascade() {
        return deleteCascade.isSelected();
    }

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public JComponent[] getValidationComponents() {
        return new JComponent[] {
                constraintName,
                referencedTableName,
                referencedColumnNames
        };
    }
}
