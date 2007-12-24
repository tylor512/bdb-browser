package org.mindhaus.bdb.browser.ui;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.mindhaus.bdb.browser.ui.views.EnvironmentView;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		layout.addView(EnvironmentView.ID,  IPageLayout.LEFT, 0.25f, editorArea);
		
		
		layout.getViewLayout(EnvironmentView.ID).setCloseable(false);
	}
}
