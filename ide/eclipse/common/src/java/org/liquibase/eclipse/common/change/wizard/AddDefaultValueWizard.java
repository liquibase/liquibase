package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.AddDefaultValueChange;
import liquibase.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddDefaultValueWizardPage;

public class AddDefaultValueWizard extends BaseRefactorWizard {

	private Column column;
	private AddDefaultValueWizardPage page1;

	public AddDefaultValueWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddDefaultValueChange change = new AddDefaultValueChange();
		change.setTableName(column.getTable().getName());
		change.setColumnName(column.getName());
		change.setDefaultValue(page1.getDefaultValue());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new AddDefaultValueWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)column).refresh();		
	}

}
