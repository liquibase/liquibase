package org.liquibase.ide.common.change.wizard.page;

public interface AddColumnWizardPage extends RefactorWizardPage {
    String getColumnName();

    String getColumnType();
}
