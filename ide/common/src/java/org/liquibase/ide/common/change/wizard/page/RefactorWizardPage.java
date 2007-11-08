package org.liquibase.ide.common.change.wizard.page;

import javax.swing.*;

public interface RefactorWizardPage {
    void init();

    JComponent getComponent();
}
