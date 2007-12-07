package org.liquibase.ide.common.change.wizard.page;

import org.liquibase.ide.common.WizardPage;

public interface ChangeMetaDataWizardPage extends WizardPage {
    String getId();

    String getAuthor();

    boolean isAlwaysRun();

    boolean isRunOnChange();

    String getContext();

    String getDbms();

    String getComments();
}
