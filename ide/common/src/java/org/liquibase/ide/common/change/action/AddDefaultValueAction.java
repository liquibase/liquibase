package org.liquibase.ide.common.change.action;

import liquibase.change.AddDefaultValueChange;
import liquibase.change.Change;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class AddDefaultValueAction extends BaseColumnRefactorAction {
    public AddDefaultValueAction() {
        super("Add Default Value");
    }

    @Override
    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Add default value to "+selectedColumn, new SingleValueWizardPageImpl("New DefaultValue"));
    }

    @Override
    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        AddDefaultValueChange change = new AddDefaultValueChange();
        change.setTableName(column.getTable().getName());
        change.setColumnName(column.getName());
        change.setDefaultValue(((SingleValueWizardPage) pages[0]).getValue());

        return new Change[] { change };
    }
}
