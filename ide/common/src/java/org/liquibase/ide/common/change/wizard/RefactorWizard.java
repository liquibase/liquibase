package org.liquibase.ide.common.change.wizard;

import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class RefactorWizard {

    private String title;
    private RefactorWizardPage[] pages;


    public RefactorWizard(String title, RefactorWizardPage... pages) {
        this.title = title;
        this.pages = pages;
    }

    public String getTitle() {
        return title;
    }

    public RefactorWizardPage[] getPages() {
        return pages;
    }
}
