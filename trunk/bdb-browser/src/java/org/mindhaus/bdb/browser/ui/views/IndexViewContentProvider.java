/**
 * 
 */
package org.mindhaus.bdb.browser.ui.views;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.mindhaus.bdb.ClassloaderUtil;
import org.mindhaus.bdb.browser.ui.model.IndexNode;
import org.mindhaus.bdb.browser.ui.model.ObjectNode;

import com.sleepycat.persist.EntityCursor;

public class IndexViewContentProvider implements IStructuredContentProvider,
		ITreeContentProvider {
	Logger log = Logger.getLogger(IndexViewContentProvider.class.getName());

	Object root;
	ClassloaderUtil cLUtil = new ClassloaderUtil();

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
		try {
			cLUtil.setClassLoader();
			List<Object> children = new ArrayList<Object>();
			if (parent instanceof IndexNode) {
				Iterable cursor = ((IndexNode) parent).getEntries();
				for (Object entity : cursor) {
					entity = new ObjectNode("Entity", entity.getClass(), entity);
					children.add(entity);
				}
			} else if (parent instanceof ObjectNode) {
				ObjectNode node = (ObjectNode) parent;
				Object value = node.getValue();
				children = _getChildren(value);
			} else {
				children = _getChildren(parent);
			}
			return children.toArray();
		} finally {
			cLUtil.restoreClassloader();
		}
	}

	

	protected List<Object> _getChildren(Object parent) {
		List<Object> children = new ArrayList<Object>();
		Field[] fields = parent.getClass().getDeclaredFields();
		if (fields != null) {
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				try {
					Object child = getChild(parent, field);
					children.add(child);
				} catch (Throwable e) {
					log.log(Level.SEVERE, "Error accessing field: " + parent
							+ " - " + field);
				}
			}
		}
		return children;
	}

	protected Object getChild(Object parent, Field field)
			throws IllegalAccessException {
		field.setAccessible(true);
		Object child = field.get(parent);
		child = new ObjectNode(field.getName(), field.getType(), child);
		return child;
	}

	public boolean hasChildren(Object parent) {
		if (parent instanceof EntityCursor) {
			return true;
		} else {
			Field[] fields = parent.getClass().getDeclaredFields();
			if (fields != null) {
				return true;
			}
		}
		return false;
	}
	
	

}