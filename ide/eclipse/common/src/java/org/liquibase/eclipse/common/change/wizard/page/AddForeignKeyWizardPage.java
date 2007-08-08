package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class AddForeignKeyWizardPage extends RefactorWizardPage {

	private Text tableName;
	private Text columnName;
	private Text constraintName;

    private Button deferrable;
    private Button initiallyDeferred;

	
	public AddForeignKeyWizardPage(Column column) {
		super("addForeignKey");
		setTitle("Add Foreign Key Constraint");
		setDescription("Add foreign key constraint to "+column.getTable().getName()+"."+column.getName());

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Constraint Name:", composite);
		constraintName = new Text(composite, SWT.BORDER);
		validateOnChange(constraintName, "Constraint Name");
		constraintName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Referenced Table:", composite);
		tableName = new Text(composite, SWT.BORDER);
		validateOnChange(tableName, "Referenced Table");
		tableName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Column Name(s):", composite);
		columnName = new Text(composite, SWT.BORDER);
		validateOnChange(columnName, "Column Name(s)");
		columnName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Deferrable:", composite);
		deferrable = new Button(composite, SWT.CHECK);
		deferrable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Initially Deferred:", composite);
		initiallyDeferred = new Button(composite, SWT.CHECK);
		initiallyDeferred.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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

	public boolean isDeferrable() {
		return deferrable.getSelection();
	}

	public boolean isInitiallyDeferred() {
		return initiallyDeferred.getSelection();
	}
}
