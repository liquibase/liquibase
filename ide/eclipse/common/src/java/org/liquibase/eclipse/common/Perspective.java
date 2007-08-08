package org.liquibase.eclipse.common;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.addView("org.eclipse.datatools.connectivity.DataSourceExplorerNavigator", IPageLayout.LEFT, 0.50f, layout.getEditorArea());
		layout.addView("org.eclipse.views.progress", IPageLayout.BOTTOM, 0.22f, layout.getEditorArea());
	}
}
