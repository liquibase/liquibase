package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddNotNullConstraintWizardPage extends RefactorWizardPage {

	private Text defaultNullValue;
	public AddNotNullConstraintWizardPage(Column column) {
		super("addNotNullConstraint");
		setTitle("Add Not Null Constraint");
		setDescription("Add not null constraint to "+column.getTable().getName()+"."+column.getName());

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Value For Current Nulls:", composite);
		defaultNullValue = new Text(composite, SWT.BORDER);
		defaultNullValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getDefaultNullValue() {
		return defaultNullValue.getText();
	}
}
