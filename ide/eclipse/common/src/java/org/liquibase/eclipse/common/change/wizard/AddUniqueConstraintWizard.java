package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.core.AddUniqueConstraintChange;
import liquibase.change.Change;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.AddUniqueConstraintWizardPage;

public class AddUniqueConstraintWizard extends BaseEclipseRefactorWizard {

	private Column column;
	private AddUniqueConstraintWizardPage page1;

	public AddUniqueConstraintWizard(Database database, Connection connection, Column column) {
		super(database, connection);
		this.column = column;
	}

	@Override
	protected Change[] createChanges() {
		AddUniqueConstraintChange change = new AddUniqueConstraintChange();
		change.setTableName(column.getTable().getName());
		change.setColumnNames(column.getName());
		change.setConstraintName(page1.getConstraintName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new AddUniqueConstraintWizardPage(column);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)column).refresh();		
	}

}
