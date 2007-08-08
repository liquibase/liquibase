package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.schema.Schema;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class CreateViewWizardPage extends RefactorWizardPage {

	private Text viewName;
	private Text viewDefinition;
	
	public CreateViewWizardPage(Schema schema) {
		super("createView");
		setTitle("Create View");
		setDescription("Create View");;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&View Name:", composite);
		viewName = new Text(composite, SWT.BORDER);
		validateOnChange(viewName, "View Name");
		viewName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Definition:", composite);
		viewDefinition = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		validateOnChange(viewDefinition, "Definition");
		viewDefinition.setSize(200, 100);
		viewDefinition.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setControl(composite);
	}

	public String getViewName() {
		return viewName.getText();
	}

	public String getViewDefinition() {
		return viewDefinition.getText();
	}
}
