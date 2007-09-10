package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.AddForeignKeyConstraintChange;
import liquibase.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddForeignKeyWizardPage;

public class AddForeignKeyWizard extends BaseRefactorWizard {

	private Column column;

	private AddForeignKeyWizardPage page1;

	public AddForeignKeyWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddForeignKeyConstraintChange change = new AddForeignKeyConstraintChange();

		change.setBaseTableName(column.getTable().getName());
		change.setBaseColumnNames(column.getName());

		change.setReferencedTableName(page1.getTableName());
		change.setReferencedColumnNames(page1.getColumnNames());

		change.setConstraintName(page1.getConstraintName());
		change.setDeferrable(page1.isDeferrable());
		change.setInitiallyDeferred(page1.isInitiallyDeferred());
		
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		page1 = new AddForeignKeyWizardPage(column);

		return new IWizardPage[] { page1, };
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)column).refresh();		
	}

}
