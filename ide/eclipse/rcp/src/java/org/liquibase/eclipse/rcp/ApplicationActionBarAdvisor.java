package org.liquibase.eclipse.rcp;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.liquibase.eclipse.rcp.action.UpdateAction;

public class ApplicationActionBarAdvisor extends ActionBarAdvisor {
	private IWorkbenchAction exitAction;

	private IWorkbenchAction cutAction;
	private IWorkbenchAction copyAction;
	private IWorkbenchAction pasteAction;

	private IWorkbenchAction aboutAction;

	private IWorkbenchAction preferencesAction;

	private IWorkbenchAction helpContentsAction;
	private IAction updateAction;

	private IAction helpSearchAction;

	private IWorkbenchAction newAction;

	private IWorkbenchAction newEditorAction;

	private IContributionItem views;

	public ApplicationActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	protected void makeActions(IWorkbenchWindow window) {
		newAction = ActionFactory.NEW.create(window);
		register(newAction);

		newEditorAction = ActionFactory.NEW_EDITOR.create(window);
		register(newEditorAction);

		exitAction = ActionFactory.QUIT.create(window);
		register(exitAction);

		copyAction = ActionFactory.COPY.create(window);
		register(copyAction);

		cutAction = ActionFactory.CUT.create(window);
		register(cutAction);

		pasteAction = ActionFactory.PASTE.create(window);
		register(pasteAction);

		preferencesAction = ActionFactory.PREFERENCES.create(window);
		register(preferencesAction);

		views = ContributionItemFactory.VIEWS_SHORTLIST.create(window);

		helpContentsAction = ActionFactory.HELP_CONTENTS.create(window);
		register(helpContentsAction);

		helpSearchAction = ActionFactory.HELP_SEARCH.create(window);
		register(helpSearchAction);

		aboutAction = ActionFactory.ABOUT.create(window);
		register(aboutAction);
		
		updateAction = new UpdateAction(window);
		register(updateAction);
	}

	protected void fillMenuBar(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager("&File",
				IWorkbenchActionConstants.M_FILE);
		fileMenu.add(newAction);
		fileMenu.add(newEditorAction);
		fileMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		fileMenu.add(preferencesAction);
		fileMenu.add(exitAction);

		MenuManager editMenu = new MenuManager("&Edit",
				IWorkbenchActionConstants.M_EDIT);
		editMenu.add(cutAction);
		editMenu.add(copyAction);
		editMenu.add(pasteAction);
		editMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));

		MenuManager windowMenu = new MenuManager("&Window",
				IWorkbenchActionConstants.M_WINDOW);
		windowMenu.add(views);

		MenuManager helpMenu = new MenuManager("&Help", "help");
		helpMenu.add(helpSearchAction);
		helpMenu.add(helpContentsAction);
		helpMenu.add(updateAction);
		helpMenu.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		helpMenu.add(aboutAction);

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuBar.add(helpMenu);
		
		removeExtraneousActions();
	}
	
	

	@SuppressWarnings("restriction")
	private void removeExtraneousActions() {

		ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();

		// removing gotoLastPosition message
		removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.navigation");

		// Removing “Convert Line Delimiters To” menu
		removeStandardAction(reg, "org.eclipse.ui.edit.text.actionSet.convertLineDelimitersTo");

		// Removing “Working Set” menu
		removeStandardAction(reg, "org.eclipse.ui.WorkingSetActionSet");
}

	@SuppressWarnings("restriction")
	private void removeStandardAction(ActionSetRegistry reg, String actionSetId) {

		IActionSetDescriptor[] actionSets = reg.getActionSets();

		for (int i = 0; i < actionSets.length; i++) {
			if (!actionSets[i].getId().equals(actionSetId)) {
				continue;
			}

			IExtension ext = actionSets[i].getConfigurationElement()
					.getDeclaringExtension();

			reg.removeExtension(ext, new Object[] { actionSets[i] });

		}

	}
}
