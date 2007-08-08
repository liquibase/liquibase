package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddDefaultValueWizardPage extends RefactorWizardPage {

	private Text defaultValue;
	private Column column;
	
	public AddDefaultValueWizardPage(Column column) {
		super("addColumn");
		setTitle("Add Default Value");
		setDescription("Add default value to "+column.getTable().getName()+"."+column.getName());
		this.column  = column;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Default Value:", composite);
		defaultValue = new Text(composite, SWT.BORDER);
		defaultValue.setText(column.getDefaultValue().replaceFirst("^'", "").replaceFirst("'$", ""));
		defaultValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getDefaultValue() {
		return defaultValue.getText();
	}
}
