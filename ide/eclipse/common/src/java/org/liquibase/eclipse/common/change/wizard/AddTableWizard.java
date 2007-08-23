package org.liquibase.eclipse.common.change.wizard;

import liquibase.migrator.change.Change;
import liquibase.migrator.change.ColumnConfig;
import liquibase.migrator.change.ConstraintsConfig;
import liquibase.migrator.change.CreateTableChange;
import org.eclipse.datatools.connectivity.sqm.core.rte.jdbc.JDBCSchema;
import org.eclipse.datatools.modelbase.sql.schema.Database;
import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.datatools.modelbase.sql.tables.PersistentTable;
import org.eclipse.datatools.modelbase.sql.tables.SQLTablesFactory;
import org.eclipse.datatools.sqltools.tablewizard.ui.DefaultTableFormModel;
import org.eclipse.datatools.sqltools.tablewizard.ui.TableFormModel;
import org.eclipse.datatools.sqltools.tablewizard.ui.wizardpages.columns.GenericColumnsPage;
import org.eclipse.datatools.sqltools.tablewizard.ui.wizardpages.pk.GenericPrimaryKeyPage;
import org.eclipse.jface.wizard.IWizardPage;

import java.sql.Connection;

@SuppressWarnings("restriction")
public class AddTableWizard extends BaseRefactorWizard {
	private GenericColumnsPage page1;
	private GenericPrimaryKeyPage page2;
	
	private PersistentTable table;
	


	public AddTableWizard(Database database, Schema schema, Connection connection) {
		super(database, connection);
		
		this.table = SQLTablesFactory.eINSTANCE.createPersistentTable();
		this.table.setName("New Table");
		this.table.setSchema(schema);
	}
	
	public IWizardPage[] createPages() {
		TableFormModel model = new DefaultTableFormModel(this.table);
		
		page1 = new GenericColumnsPage("org.eclipse.datatools.sqltools.tablewizard.ui.GenericColumnPage", model.getPersistentTable());		
		page2 = new GenericPrimaryKeyPage("org.eclipse.datatools.sqltools.tablewizard.ui.GenericPrimaryKeyPage", model.getPersistentTable());

		return new IWizardPage[] {page1, page2};

	}

	@Override
	protected Change[] createChanges() {
		CreateTableChange change = new CreateTableChange();
		change.setTableName(table.getName());
		for (Object columnAsObject : table.getColumns()) {
			Column column = (Column) columnAsObject;
			
			ColumnConfig columnConfig = new ColumnConfig();
			columnConfig.setName(column.getName());
			columnConfig.setType(column.getDataType().getName());
			columnConfig.setDefaultValue(column.getDefaultValue());
			//TODO: columnConfig.setAutoIncrement(column.is)

			ConstraintsConfig constraints = new ConstraintsConfig();
			constraints.setNullable(column.isNullable());
			constraints.setPrimaryKey(column.isPartOfPrimaryKey());
//			constraints.setUnique(column.isPartOfUniqueConstraint());
//			constraints.setForeignKeyName(foreignKeyName)
			
			columnConfig.setConstraints(constraints);
			change.addColumn(columnConfig);
		}

		return new Change[] {change};
	}
	
	@Override
	protected void refresh() {
		((JDBCSchema)table.getSchema()).refresh();		
	}
}
