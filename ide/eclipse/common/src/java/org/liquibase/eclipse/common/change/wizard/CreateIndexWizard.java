package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;
import java.util.Arrays;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.ColumnConfig;
import liquibase.migrator.change.CreateIndexChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCColumn;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.CreateIndexWizardPage;

public class CreateIndexWizard extends BaseRefactorWizard {

	private Column column;
	private CreateIndexWizardPage page1;

	public CreateIndexWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		CreateIndexChange change = new CreateIndexChange();
		change.setTableName(column.getTable().getName());
		
		ColumnConfig config = new ColumnConfig();
		config.setName(column.getName());
		config.setType(column.getType().getName());
		
		change.setColumns(Arrays.asList(new ColumnConfig[] {config}));
		change.setIndexName(page1.getIndexName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new CreateIndexWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((JDBCColumn)column).refresh();		
	}

}
