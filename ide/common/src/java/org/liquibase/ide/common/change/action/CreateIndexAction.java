package org.liquibase.ide.common.change.action;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.CreateIndexChange;
import liquibase.database.structure.Column;
import org.liquibase.ide.common.change.wizard.RefactorWizard;
import org.liquibase.ide.common.change.wizard.page.RefactorWizardPage;
import org.liquibase.ide.common.change.wizard.page.SingleValueWizardPageImpl;

public class CreateIndexAction extends BaseColumnRefactorAction {
    public CreateIndexAction() {
        super("Create Index");
    }

    public RefactorWizard createRefactorWizard(Column selectedColumn) {
        return new RefactorWizard("Create index on "+selectedColumn, new SingleValueWizardPageImpl("Index Name"));
    }

    protected Change[] createChanges(Column column, RefactorWizardPage... pages) {
        CreateIndexChange change = new CreateIndexChange();
        change.setTableName(column.getTable().getName());

        ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setName(column.getName());

        change.addColumn(columnConfig);
        change.setIndexName(((SingleValueWizardPageImpl) pages[0]).getValue());

        return new Change[] { change };
    }

}
