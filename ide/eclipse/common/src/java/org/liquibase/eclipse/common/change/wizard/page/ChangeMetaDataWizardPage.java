package org.liquibase.eclipse.common.change.wizard.page;

import liquibase.util.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.liquibase.eclipse.common.LiquibasePreferences;

import java.util.Date;

public class ChangeMetaDataWizardPage extends RefactorWizardPage {

    private Text id;
	private Text author;

    private Button alwaysRun;
    private Button runOnChange;
    private Text context;
    private Text dbms;
    private Text comments;

	
	
	public ChangeMetaDataWizardPage() {
		super("metaData");
		setTitle("Change Set Information");
		setDescription("Set Additional Change Information");

	}

	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		addLabel("&Change Log:", composite);
		
		Composite changeLogSelector = new Composite(composite, SWT.NONE);
		changeLogSelector.setLayout(new GridLayout(2, false));
		
		Label changeLogLabel = new Label(changeLogSelector, SWT.NONE);
		changeLogLabel.setText(LiquibasePreferences.getCurrentChangeLog());
		changeLogLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
		
		Button selectChangeLogButton = new Button(changeLogSelector, SWT.PUSH);
		selectChangeLogButton.setText("Select");
		selectChangeLogButton.setToolTipText("Select change log to save change to");
		selectChangeLogButton.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
		selectChangeLogButton.addSelectionListener(new SelectFileListener(changeLogLabel));
		
		changeLogSelector.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));


		addLabel("&Id:", composite);
		id = new Text(composite, SWT.BORDER);
		id.setText(String.valueOf(new Date().getTime()));
		id.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		
		addLabel("&Author:", composite);
		author = new Text(composite, SWT.BORDER);
		author.setText(StringUtils.trimToEmpty(System.getProperty("user.name")));
		author.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Run On Change:", composite);
		runOnChange = new Button(composite, SWT.CHECK);
		runOnChange.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Always Run:", composite);
		alwaysRun = new Button(composite, SWT.CHECK);
		alwaysRun.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Context(s):", composite);
		context = new Text(composite, SWT.BORDER);
		context.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&DBMS(s):", composite);
		dbms = new Text(composite, SWT.BORDER);
		dbms.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		addLabel("&Comments:", composite);
		comments = new Text(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		comments.setSize(200, 100);
		comments.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		setControl(composite);
	}

	public String getId() {
		return id.getText();
	}

	public String getAuthor() {
		return author.getText();
	}

	public boolean isAlwaysRun() {
		return alwaysRun.getSelection();
	}

	public boolean isRunOnChange() {
		return runOnChange.getSelection();
	}

	public String getContext() {
		return context.getText();
	}

	public String getDbms() {
		return dbms.getText();
	}

	public String getComments() {
		return comments.getText();
	}
	
	private static class SelectFileListener implements SelectionListener {

		private Label changeLogLabel;

		public SelectFileListener(Label changeLogLabel) {
			this.changeLogLabel = changeLogLabel; 
		}

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		public void widgetSelected(SelectionEvent e) {
//			FileDialog fd = new FileDialog(changeLogLabel.getShell(), SWT.OPEN);
//	        fd.setText("Select Current Change Log");
//	        String[] filterExt = { "*.xml", "*.*" };
//	        fd.setFilterExtensions(filterExt);
//	        fd.setFileName(LiquibasePreferences.getCurrentChangeLogFileName());
//	        String selected = fd.open();

			PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(changeLogLabel.getShell(), LiquibasePreferences.PREFERENCES_ID, null, null);
			if (dialog.open() == Dialog.OK) {
//	        if (selected != null) {
//	        	LiquibasePreferences.setCurrentChangeLogFileName(selected);
	        	changeLogLabel.setText(LiquibasePreferences.getCurrentChangeLog());
//	        }
			}
		}
		
	}

}
