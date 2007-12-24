/**
 * 
 */
package org.mindhaus.bdb.browser.ui.views;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.mindhaus.bdb.browser.ui.model.IndexNode;

import com.sleepycat.persist.EntityCursor;

public class IndexViewContentProvider implements IStructuredContentProvider, 
									   ITreeContentProvider {
	Logger log = Logger.getLogger(IndexViewContentProvider.class.getName());
	
	Object root;

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    	root = newInput;
	}
    
	public void dispose() {
	}
    
	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}
    
	public Object getParent(Object child) {
		return root;
	}
    
	public Object[] getChildren(Object parent) {
		List<Object> children = new ArrayList<Object>();
		if (parent instanceof IndexNode) {			
			Iterable cursor = ((IndexNode)parent).getEntries();
			for (Object entity : cursor) {
				children.add(entity);
			}
		} else {
			
			Field[] fields = parent.getClass().getDeclaredFields();
    		if (fields != null ) {
				for (int i = 0; i < fields.length; i++) {
					Field field = fields[i];
					try {
						field.setAccessible(true);
						Object child = field.get(parent);
						children.add(child);
					} catch (Throwable e) {
						log.log(Level.SEVERE, "Error accessing field: " + parent + " - " + field);
					}
				}
    		}
		}
		return children.toArray();
	}

    public boolean hasChildren(Object parent) {
    	if (parent instanceof EntityCursor) { 
			return true;
    	} else {
    		Field[] fields = parent.getClass().getDeclaredFields();
    		if (fields != null ) {
    			return true;
    		}
    	}
    	return false;
	}
}