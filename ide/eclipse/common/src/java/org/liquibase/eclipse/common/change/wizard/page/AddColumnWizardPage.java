package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddColumnWizardPage extends RefactorWizardPage {

    private Text columnName;
	private Text dataType;
	private Text defaultValue;
	
	public AddColumnWizardPage(String tableName) {
		super("addColumn");
		setTitle("Add Column");
		setDescription("Add Column to "+tableName);

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Column Name:", composite);
		columnName = new Text(composite, SWT.BORDER);
		validateOnChange(columnName, "Column Name");
		columnName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		addLabel("&Data Type:", composite);
		dataType = new Text(composite, SWT.BORDER);
		validateOnChange(dataType, "Data Type");
		dataType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Default Value:", composite);
		defaultValue = new Text(composite, SWT.BORDER);
		defaultValue.setText("NULL");
		defaultValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getColumnName() {
		return columnName.getText();
	}

	public String getDataType() {
		return dataType.getText();
	}

	public String getDefaultValue() {
		return defaultValue.getText();
	}
}
