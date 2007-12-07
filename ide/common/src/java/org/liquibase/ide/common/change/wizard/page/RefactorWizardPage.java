package org.liquibase.ide.common.change.wizard.page;

import org.liquibase.ide.common.WizardPage;

import javax.swing.*;

public interface RefactorWizardPage extends WizardPage {
    void init();

    JComponent getComponent();
}
