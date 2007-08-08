package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Table;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RenameTableWizardPage extends RefactorWizardPage {

	private Text newTableName;
	private Table table;
	
	public RenameTableWizardPage(Table table) {
		super("renameTable");
		setTitle("Rename Table");
		setDescription("Rename table "+table.getName());
		this.table  = table;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&New Table Name:", composite);
		newTableName = new Text(composite, SWT.BORDER);
		validateOnChange(newTableName, "New Table Name");
		newTableName.setText(table.getName());
		newTableName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getNewTableName() {
		return newTableName.getText();
	}
}
