package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.core.AddNotNullConstraintChange;
import liquibase.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddNotNullConstraintWizardPage;

public class AddNotNullConstraintWizard extends BaseEclipseRefactorWizard {

	private Column column;
	private AddNotNullConstraintWizardPage page1;


	public AddNotNullConstraintWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddNotNullConstraintChange change = new AddNotNullConstraintChange();
		change.setTableName(column.getTable().getName());
		change.setColumnName(column.getName());
		change.setDefaultNullValue(page1.getDefaultNullValue());
		change.setColumnDataType(column.getType().getName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {		
		this.page1 = new AddNotNullConstraintWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)column).refresh();		
	}

}
