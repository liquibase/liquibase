package org.liquibase.intellij.plugin.change.wizard.page;

import com.intellij.ide.wizard.Step;
import com.intellij.ide.wizard.CommitStepException;

import javax.swing.*;

import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class IntellijRefactorWizardPage implements Step {

    private RefactorWizardPage refactorPage;

    public IntellijRefactorWizardPage(RefactorWizardPage refactorPage) {
        this.refactorPage = refactorPage;
    }


    public void _init() {
        refactorPage.init();
    }

    public void _commit(boolean b) throws CommitStepException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Icon getIcon() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public JComponent getComponent() {
        return refactorPage.getComponent();
    }
}
