package org.liquibase.ide.common.change.wizard.page;

public interface CreateViewWizardPage extends RefactorWizardPage {
    public String getViewName();
    public String getViewDefinition();
}
