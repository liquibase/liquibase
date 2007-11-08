package org.liquibase.ide.common.change.wizard.page;

public interface ChangeMetaDataWizardPage {
    String getId();

    String getAuthor();

    boolean isAlwaysRun();

    boolean isRunOnChange();

    String getContext();

    String getDbms();

    String getComments();
}
