package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.AddLookupTableChange;
import liquibase.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddLookupTableWizardPage;

public class AddLookupTableWizard extends BaseEclipseRefactorWizard {

	private Column column;

	private AddLookupTableWizardPage page1;

	public AddLookupTableWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddLookupTableChange change = new AddLookupTableChange();

		change.setExistingTableName(column.getTable().getName());
		change.setExistingColumnName(column.getName());

		change.setNewTableName(page1.getTableName());
		change.setNewColumnName(page1.getColumnNames());

		change.setConstraintName(page1.getConstraintName());
		change.setNewColumnDataType(column.getDataType().getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		page1 = new AddLookupTableWizardPage(column);

		return new IWizardPage[] { page1, };
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)column).refresh();		
	}

}
