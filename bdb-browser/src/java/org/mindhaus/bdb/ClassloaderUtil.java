package org.mindhaus.bdb;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ClassloaderUtil {


	ClassLoader eclipseClassloader;
	
	public void restoreClassloader() {
		Thread.currentThread().setContextClassLoader(eclipseClassloader);
	}

	public void setClassLoader() {
		eclipseClassloader = Thread.currentThread().getContextClassLoader();
		URLClassLoader loader = null;

		URL[] urls = getClasspathAsURLArray();

		loader = new URLClassLoader(urls, eclipseClassloader);

		Thread.currentThread().setContextClassLoader(loader);
	}

	public static URL[] getClasspathAsURLArray() {
		Set visited = new HashSet();
		List urls = new ArrayList(50);
		IJavaProject[] projects = null;
		try {
			projects = JavaCore
					.create(ResourcesPlugin.getWorkspace().getRoot())
					.getJavaProjects();
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (IJavaProject project : projects) {
			collectClasspathURLs(project, urls, visited, true);
		}

		URL[] result = new URL[urls.size()];
		urls.toArray(result);
		return result;
	}

	private static void collectClasspathURLs(IJavaProject javaProject,
			List urls, Set visited, boolean isFirstProject) {
		if (visited.contains(javaProject))
			return;
		visited.add(javaProject);
		IPath outPath = null;
		IClasspathEntry[] entries = null;
		try {
			outPath = javaProject.getOutputLocation();
			IFile outDir = ResourcesPlugin.getWorkspace().getRoot().getFile(outPath);
			if (outDir != null) { 
				outPath = outDir.getLocation();
				outPath = outPath.addTrailingSeparator();
			    URL out = createFileURL(outPath);
				urls.add(out);				
			}			
			entries = javaProject.getResolvedClasspath(true);
		} catch (JavaModelException e) {
			return;
		}
		IClasspathEntry entry, resEntry;
		IJavaProject proj = null;
		List projects = null;
		for (int i = 0; i < entries.length; i++) {
			entry = entries[i];
			switch (entry.getEntryKind()) {
			case IClasspathEntry.CPE_LIBRARY:
			case IClasspathEntry.CPE_CONTAINER:
			case IClasspathEntry.CPE_VARIABLE:
				collectClasspathEntryURL(entry, urls);
				break;
			case IClasspathEntry.CPE_PROJECT: {

				break;
			}
			}
		}
	}

	private static URL createFileURL(IPath path) {
		URL url = null;
		try {
			url = new URL("file://" + path.toOSString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}

	private static void collectClasspathEntryURL(IClasspathEntry entry,
			List urls) {
		URL url = createFileURL(entry.getPath());
		if (url != null)
			urls.add(url);
	}
}
