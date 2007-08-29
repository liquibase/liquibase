package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.CreateViewChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.CreateViewWizardPage;

public class CreateViewWizard extends BaseRefactorWizard {
	private CreateViewWizardPage page1;
	private Schema schema;

	public CreateViewWizard(Database database, Schema schema, Connection connection) {
		super(database, connection);
		this.schema = schema;
	}
	
	public IWizardPage[] createPages() {
		page1 = new CreateViewWizardPage(schema);		

		return new IWizardPage[] {page1};

	}

	@Override
	protected Change[] createChanges() {
		CreateViewChange change = new CreateViewChange();
		change.setViewName(page1.getViewName());
		change.setSelectQuery(page1.getViewDefinition());
		
		return new Change[] { change };
	}
	
	@Override
	protected void refresh() {
		((ICatalogObject)schema).refresh();		
	}
}
