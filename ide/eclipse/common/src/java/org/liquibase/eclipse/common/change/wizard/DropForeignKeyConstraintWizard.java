package org.liquibase.eclipse.common.change.wizard;

import java.sql.Connection;

import liquibase.change.Change;
import liquibase.change.core.DropForeignKeyConstraintChange;

import org.eclipse.datatools.connectivity.sqm.core.rte.ICatalogObject;
import org.eclipse.datatools.modelbase.sql.constraints.ForeignKey;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.jface.wizard.IWizardPage;

public class DropForeignKeyConstraintWizard extends BaseEclipseRefactorWizard {

	private ForeignKey fk;
	private Table baseTable;
	private Table referencedTable;
	
	public DropForeignKeyConstraintWizard(Database database, Connection connection, ForeignKey fk) {
		super(database, connection);
		this.fk = fk;
		this.baseTable = fk.getBaseTable();
		this.referencedTable = fk.getReferencedTable();
	}

	@Override
	protected Change[] createChanges() {
		DropForeignKeyConstraintChange change = new DropForeignKeyConstraintChange();
		change.setBaseTableName(fk.getBaseTable().getName());
		change.setConstraintName(fk.getName());
		
		return new Change[] { change };
	}

	@Override
	protected IWizardPage[] createPages() {
		return new IWizardPage[0];
	}

	@Override
	protected void refresh() {
		((ICatalogObject)baseTable).refresh();
		((ICatalogObject)referencedTable).refresh();
	}
}
