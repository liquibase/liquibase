package org.liquibase.eclipse.common.migrator.wizard;

import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.jface.wizard.Wizard;

import java.sql.Connection;

public class RollbackWizard extends Wizard {

	public RollbackWizard(Database selectedDatabase, Connection selectedConnection) {
	}

	@Override
	public void addPages() {
//		addPage(page)
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}

	
}
