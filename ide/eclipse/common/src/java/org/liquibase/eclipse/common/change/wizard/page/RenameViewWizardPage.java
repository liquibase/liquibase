package org.liquibase.eclipse.common.change.wizard.page;

import org.eclipse.datatools.modelbase.sql.tables.ViewTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RenameViewWizardPage extends RefactorWizardPage {

	private Text newViewName;
	private ViewTable view;
	
	public RenameViewWizardPage(ViewTable view) {
		super("renameView");
		setTitle("Rename View");
		setDescription("Rename view "+view.getName());
		this.view  = view;

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&New View Name:", composite);
		newViewName = new Text(composite, SWT.BORDER);
		validateOnChange(newViewName, "New View Name");
		newViewName.setText(view.getName());
		newViewName.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		setControl(composite);
	}

	public String getNewViewName() {
		return newViewName.getText();
	}
}
