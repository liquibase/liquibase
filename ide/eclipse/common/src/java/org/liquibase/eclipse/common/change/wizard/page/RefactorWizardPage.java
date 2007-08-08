package org.liquibase.eclipse.common.change.wizard.page;

import java.util.ArrayList;
import java.util.List;

import liquibase.util.StringUtils;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public abstract class RefactorWizardPage extends WizardPage {
	
	private List<Validator> validators = new ArrayList<Validator>();
	
	protected RefactorWizardPage(String pageName) {
		super(pageName);
	}

	protected void addLabel(String labelText, Composite composite) {
		Label auhorLabel = new Label(composite, SWT.NONE);
		auhorLabel.setText(labelText);
		auhorLabel.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false));
	}

	@Override
	protected void setControl(Control newControl) {
		super.setControl(newControl);
		validate();
	}
	protected void validateOnChange(Text text, String columnName) {
		validators.add(new Validator(text, columnName));
		
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validate();				
			}
		});
	}
	
	protected void validate() {
		for (Validator validator : validators) {
			if (!validator.isValid()) {
				setErrorMessage(validator.getMessage());
				setPageComplete(false);
				
				return;
			}
		}
		setErrorMessage(null);
		setPageComplete(true);
	}
	
	private static class Validator {
		private Text text;
		private String name;
		
		public Validator(Text text, String name) {
			this.text = text;
			this.name = name;
		}
		
		public boolean isValid() {
			return StringUtils.trimToNull(text.getText()) != null;
		}

		public String getMessage() {
			return name+" is required";
		}
	}
	

}
