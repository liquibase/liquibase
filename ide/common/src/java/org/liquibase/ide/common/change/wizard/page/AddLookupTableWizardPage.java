package org.liquibase.ide.common.change.wizard.page;

public interface AddLookupTableWizardPage extends RefactorWizardPage {
    String getConstraintName();

    String getNewTableName();

    String getNewColumnName();
}
