package org.liquibase.eclipse.common.migrator.wizard;

import java.sql.Connection;

import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.liquibase.eclipse.common.change.wizard.page.AddColumnWizardPage;

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
