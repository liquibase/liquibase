package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.Column;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CreateIndexWizardPage extends RefactorWizardPage {

	private Text indexName;
	private Column column;
	
	public CreateIndexWizardPage(Column column) {
		super("createIndex");
		setTitle("Create Index");
		setDescription("Create index on "+column.getTable().getName()+"."+column.getName());
		this.column  = column;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Index Name:", composite);
		indexName = new Text(composite, SWT.BORDER);
		validateOnChange(indexName, "Index Name");
		indexName.setText(("idx_"+column.getTable().getName()+"_"+column.getName()).toUpperCase());
		indexName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getIndexName() {
		return indexName.getText();
	}
}
