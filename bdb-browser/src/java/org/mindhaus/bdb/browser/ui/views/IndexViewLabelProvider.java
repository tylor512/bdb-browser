/**
 * 
 */
package org.mindhaus.bdb.browser.ui.views;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.mindhaus.bdb.browser.ui.model.EnvironmentNode;

public class IndexViewLabelProvider extends LabelProvider {

	/**
	 * @param view
	 */
	public IndexViewLabelProvider() {
	}
	public String getText(Object obj) {
		return obj.toString();
	}
	public Image getImage(Object obj) {
		String imageKey = ISharedImages.IMG_OBJ_ELEMENT;
		if (obj instanceof EnvironmentNode)
		   imageKey = ISharedImages.IMG_OBJ_FOLDER;
		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}