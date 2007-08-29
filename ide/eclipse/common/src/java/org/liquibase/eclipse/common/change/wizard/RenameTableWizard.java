package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.RenameTableChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.RenameTableWizardPage;

public class RenameTableWizard extends BaseRefactorWizard {

	private Table table;
	private RenameTableWizardPage page1;

	public RenameTableWizard(Database database, Connection connection, Table table) {
		super(database, connection);
		this.table = table;
	}

	@Override
	protected Change[] createChanges() {
		RenameTableChange change = new RenameTableChange();
		change.setOldTableName(table.getName());
		change.setNewTableName(page1.getNewTableName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new RenameTableWizardPage(table);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)table.getSchema()).refresh();		
	}

}
