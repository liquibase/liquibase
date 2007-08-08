package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RenameColumnWizardPage extends RefactorWizardPage {

	private Text newColumnName;
	private Column column;
	
	public RenameColumnWizardPage(Column column) {
		super("renameColumn");
		setTitle("Rename Column");
		setDescription("Rename column "+column.getTable().getName()+"."+column.getName());
		this.column  = column;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&New Column Name:", composite);
		newColumnName = new Text(composite, SWT.BORDER);
		validateOnChange(newColumnName, "New Column Name");
		newColumnName.setText(column.getName());
		newColumnName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getNewColumnName() {
		return newColumnName.getText();
	}
}
