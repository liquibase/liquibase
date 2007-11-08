package org.liquibase.ide.common.change.wizard.page;

import liquibase.change.ColumnConfig;

public interface AddTableWizardPage extends RefactorWizardPage {
    String getTableName();

    ColumnConfig[] getColumns();
}
