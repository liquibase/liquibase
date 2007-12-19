package org.liquibase.ide.common;

import liquibase.util.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;

public abstract class AbstractWizardPageWithRequiredFields implements WizardPage {

    public boolean isValid() {
        boolean isValid = true;
        for (final JComponent component : getValidationComponents()) {
            if (component == null) {
                continue;
            }
            if (StringUtils.trimToNull(((JTextComponent) component).getText()) == null) {
                isValid = false;
                component.setBackground(Color.PINK);
            } else {
                component.setBackground(Color.WHITE);
            }
        }
        return isValid;
    }
}
