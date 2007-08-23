package org.liquibase.eclipse.common.change.wizard;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.RenameColumnChange;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCTable;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.RenameColumnWizardPage;

import java.sql.Connection;

public class RenameColumnWizard extends BaseRefactorWizard {

	private Column column;
	private RenameColumnWizardPage page1;

	public RenameColumnWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		RenameColumnChange change = new RenameColumnChange();
		change.setTableName(column.getTable().getName());
		change.setOldColumnName(column.getName());
		change.setNewColumnName(page1.getNewColumnName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new RenameColumnWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((JDBCTable)column.getTable()).refresh();		
	}

}
