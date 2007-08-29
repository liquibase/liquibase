package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.DropDefaultValueChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;

public class DropDefaultValueWizard extends BaseRefactorWizard {

	private Column column;
	
	public DropDefaultValueWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		DropDefaultValueChange change = new DropDefaultValueChange();
		change.setTableName(column.getTable().getName());
		change.setColumnName(column.getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		return new IWizardPage[0];
	}

	@Override
	protected void refresh() {
		((ICatalogObject)column.getTable()).refresh();		
	}
}
