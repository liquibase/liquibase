package org.liquibase.ide.common.change.wizard.page;

import org.liquibase.ide.common.AbstractWizardPageWithRequiredFields;

import javax.swing.*;

public class CreateViewWizardPageImpl extends AbstractWizardPageWithRequiredFields implements CreateViewWizardPage {
    private JTextArea definition;
    private JTextField viewName;
    private JPanel mainPanel;

    public void init() {
        ;
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public String getViewName() {
        return viewName.getText();
    }

    public String getViewDefinition() {
        return definition.getText();
    }

    public JComponent[] getValidationComponents() {
        return new JComponent[] {
                viewName,
                definition,
        };
    }
}
