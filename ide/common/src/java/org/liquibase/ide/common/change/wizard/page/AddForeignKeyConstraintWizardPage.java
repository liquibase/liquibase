package org.liquibase.ide.common.change.wizard.page;

public interface AddForeignKeyConstraintWizardPage extends RefactorWizardPage {
    String getConstraintName();

    String getReferencedTableName();

    String getReferencedColumnNames();

    boolean getInitiallyDeferred();

    boolean getDeferrable();

    boolean getDeleteCascade();
}
