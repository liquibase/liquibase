package org.liquibase.ide.common;

import javax.swing.*;

public interface WizardPage {
    boolean isValid();

    JComponent[] getValidationComponents();
    
}
