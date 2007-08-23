package org.liquibase.eclipse.common.change.wizard;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.RenameViewChange;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.jface.wizard.IWizardPage;
import org.liquibase.eclipse.common.change.wizard.page.RenameViewWizardPage;

import java.sql.Connection;

public class RenameViewWizard extends BaseRefactorWizard {

	private ViewTable view;
	private RenameViewWizardPage page1;

	public RenameViewWizard(Database database, Connection connection, ViewTable view) {
		super(database, connection);
		this.view = view;
	}

	@Override
	protected Change[] createChanges() {
		RenameViewChange change = new RenameViewChange();
		change.setOldViewName(view.getName());
		change.setNewViewName(page1.getNewViewName());
		
		return new Change[] {
				change
		};
	}

	@Override
	protected IWizardPage[] createPages() {
		this.page1 = new RenameViewWizardPage(view);
		
		return new IWizardPage[] {
				page1,
		};
	}
	
	@Override
	protected void refresh() {
		((JDBCSchema)view.getSchema()).refresh();		
	}

}
