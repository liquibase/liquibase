package org.liquibase.eclipse.common.migrator.dialog;

import liquibase.util.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class TagDatabaseDialog extends Dialog {

	private Text tagNameText;
	private String tagName;
	
	public TagDatabaseDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Default Value:", composite);
		tagNameText = new Text(composite, SWT.BORDER);
		tagNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		composite.pack();

		return composite;
	}

	protected void addLabel(String labelText, Composite composite) {
		Label auhorLabel = new Label(composite, SWT.NONE);
		auhorLabel.setText(labelText);
		auhorLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Tag Database");
	}

	@Override
    protected void okPressed() {
		this.tagName = StringUtils.trimToNull(tagNameText.getText());
		super.okPressed();
	}
	public String getTagName() {
		return tagName;
	}
	
	
}
