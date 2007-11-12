package org.liquibase.ide.common.change.action;

import liquibase.change.AddDefaultValueChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.AddDefaultValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.AddDefaultValueWizardPageImpl;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;

public class AddDefaultValueAction extends BaseColumnRefactorAction {
    public AddDefaultValueAction() {
        super("Add Default Value");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add default value to "+selectedColumn, new AddDefaultValueWizardPageImpl());
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setDefaultValue(((AddDefaultValueWizardPage) pages[0]).getDefaultValue());

        return new Change[] { change };
    }
}
