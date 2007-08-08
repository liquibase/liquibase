package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddLookupTableWizardPage extends RefactorWizardPage {

	private Text tableName;
	private Text columnName;
	private Text constraintName;
	
	public AddLookupTableWizardPage(Column column) {
		super("addLookupTable");
		setTitle("Create Lookup Table");
		setDescription("Create lookup table from  to "+column.getTable().getName()+"."+column.getName());

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&New Table Name:", composite);
		tableName = new Text(composite, SWT.BORDER);
		validateOnChange(tableName, "New Table Name");
		tableName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Column Name:", composite);
		columnName = new Text(composite, SWT.BORDER);
		validateOnChange(columnName, "Column Name");
		columnName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Constraint Name:", composite);
		constraintName = new Text(composite, SWT.BORDER);
		validateOnChange(constraintName, "Constraint Name");
		constraintName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getTableName() {
		return tableName.getText();
	}

	public String getColumnNames() {
		return columnName.getText();
	}
	
	public String getConstraintName() {
		return columnName.getText();
	}
}
