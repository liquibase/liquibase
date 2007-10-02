package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.Change;
import liquibase.change.DropIndexChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.constraints.Index;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.jface.wizard.IWizardPage;

public class DropIndexWizard extends BaseEclipseRefactorWizard {

	private Index index;
	private Table table;
	
	public DropIndexWizard(Database database, Connection connection, Index index) {
		super(database, connection);
		this.index = index;
	}

	@Override
	protected Change[] createChanges() {
		DropIndexChange change = new DropIndexChange();
		change.setIndexName(index.getName());
		change.setTableName(table.getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		return new IWizardPage[0];
	}

	@Override
	protected void refresh() {
		((ICatalogObject)table).refresh();
	}
}
