package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.Change;
import liquibase.change.core.DropTableChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.jface.wizard.IWizardPage;

public class DropTableWizard extends BaseEclipseRefactorWizard {

	private Table table;
	
	public DropTableWizard(Database database, Connection connection, Table table) {
		super(database, connection);
		this.table = table;
	}

	@Override
	protected Change[] createChanges() {
		DropTableChange change = new DropTableChange();
		change.setTableName(table.getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		return new IWizardPage[0];
	}

	@Override
	protected void refresh() {
		((ICatalogObject)table.getSchema()).refresh();		
	}
}
