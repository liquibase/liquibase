package org.liquibase.eclipse.common.change.wizard;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.DropViewChange;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.jface.wizard.IWizardPage;

import java.sql.Connection;

public class DropViewWizard extends BaseRefactorWizard {

	private ViewTable view;
	
	public DropViewWizard(Database database, Connection connection, ViewTable view) {
		super(database, connection);
		this.view = view;
	}

	@Override
	protected Change[] createChanges() {
		DropViewChange change = new DropViewChange();
		change.setViewName(view.getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		return new IWizardPage[0];
	}

	@Override
	protected void refresh() {
		((JDBCSchema)view.getSchema()).refresh();		
	}
}
