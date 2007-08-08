package org.liquibase.eclipse.rcp.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.update.ui.UpdateJob;
import org.eclipse.update.ui.UpdateManagerUI;
import org.liquibase.eclipse.rcp.Application;

public class UpdateAction extends Action implements IAction {

	private IWorkbenchWindow window;
	
	public UpdateAction(IWorkbenchWindow window) {
		this.window = window;
		
		setId("org.liquibase.rcp.newUpdates");
		setText("&Update...");
		setToolTipText("Search for updates to LiquiBase");
		setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Application.PLUGIN_ID, "icons/usearch_obj.gif"));
		
		window.getWorkbench().getHelpSystem().setHelp(this, "org.liquibase.rcp.updates");
	}
	
	@Override
	public void run() {
		BusyIndicator.showWhile(window.getShell().getDisplay(), new Runnable() {

			public void run() {
				UpdateJob job = new UpdateJob("Searching for updates", false, false);
				UpdateManagerUI.openInstaller(window.getShell(), job);
				
			}
			
		});
	}
}
