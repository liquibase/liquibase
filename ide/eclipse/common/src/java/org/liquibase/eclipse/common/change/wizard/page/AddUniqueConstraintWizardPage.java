package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddUniqueConstraintWizardPage extends RefactorWizardPage {

	private Text constraintName;
	private Column column;
	
	public AddUniqueConstraintWizardPage(Column column) {
		super("addUniqueConstraint");
		setTitle("Add Unique Constraint");
		setDescription("Add unique constraint to "+column.getTable().getName()+"."+column.getName());
		this.column  = column;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Constraint Name:", composite);
		constraintName = new Text(composite, SWT.BORDER);
		validateOnChange(constraintName, "Constraint Name");
		constraintName.setText(("uk_"+column.getTable().getName()+"_"+column.getName()).toUpperCase());
		constraintName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getConstraintName() {
		return constraintName.getText();
	}
}
