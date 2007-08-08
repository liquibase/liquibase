package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.migrator.change.AddPrimaryKeyChange;
import liquibase.migrator.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCColumn;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddPrimaryKeyWizardPage;

public class AddPrimaryKeyWizard extends BaseRefactorWizard {

	private Column column;
	private AddPrimaryKeyWizardPage page1;

	public AddPrimaryKeyWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddPrimaryKeyChange change = new AddPrimaryKeyChange();
		change.setTableName(column.getTable().getName());
		change.setColumnNames(column.getName());
		change.setConstraintName(page1.getConstraintName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new AddPrimaryKeyWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((JDBCColumn)column).refresh();		
	}

}
