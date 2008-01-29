package org.mindhaus.bdb.browser.ui.views;

import java.io.File;
import java.util.Set;

import org.eclipse.core.commands.operations.OperationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.mindhaus.bdb.ClassloaderUtil;
import org.mindhaus.bdb.EnvironmentConnection;
import org.mindhaus.bdb.browser.ui.Activator;
import org.mindhaus.bdb.browser.ui.model.EnvironmentNode;
import org.mindhaus.bdb.browser.ui.model.IndexNode;
import org.mindhaus.bdb.browser.ui.model.Node;

import com.sleepycat.persist.PrimaryIndex;

/**
 * @todo doc
 * 
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class EnvironmentView extends ViewPart {

	public static final String ID = "bdb.browser.EnvironmentView";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action openEnvironmentAction;
	private Action doubleClickAction;
	private Node root = new Node("root");
	ClassloaderUtil cLUtil = new ClassloaderUtil();


	/**
	 * 
	 * We will set up a dummy model to initialize tree hierarchy. In real code,
	 * you will connect to a real model and expose its hierarchy.
	 */
	private void openEnvironment(String envHome) {
		try {
			cLUtil.setClassLoader();
			File dbHome = new File(envHome);
			EnvironmentConnection conn = new EnvironmentConnection(dbHome);
			EnvironmentNode envNode = new EnvironmentNode(conn);
			conn.open();
			Set<String> storeNames = conn.getEntityStores();
			for (String storeName : storeNames) {
				Node storeNode = new Node(storeName);
				envNode.addChild(storeNode);
				Set<String> entityNames = conn.getStoredEntities(storeName);
				for (String entityName : entityNames) {
					Node entityNode = new Node(entityName);
					storeNode.addChild(entityNode);
					PrimaryIndex primaryIndex = conn.getPKIndex(storeName,
							entityName);
					IndexNode pkIndexNode = new IndexNode(primaryIndex);
					entityNode.addChild(pkIndexNode);

				}

			}
			root.addChild(envNode);
		} catch (Exception e) {
			ErrorDialog error = new ErrorDialog(getSite().getShell(),
					"Environment Error",
					e.getMessage(),
					new OperationStatus(IStatus.ERROR, Activator.PLUGIN_ID, 0, e.getMessage(), e),
					IStatus.ERROR);
			error.open();
		}finally {
			cLUtil.restoreClassloader();
		}
		viewer.refresh();

	}

	/**
	 * The constructor.
	 */
	public EnvironmentView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new EnvironmentViewContentProvider());
		viewer.setLabelProvider(new EnvironmentViewLabelProvider());
		viewer.setInput(root);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				EnvironmentView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(openEnvironmentAction);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(openEnvironmentAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(openEnvironmentAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		openEnvironmentAction = new Action() {
			public void run() {
				DirectoryDialog d = new DirectoryDialog(getSite().getShell(),
						SWT.OPEN);
				String envHome = d.open();
				openEnvironment(envHome);
			}
		};
		openEnvironmentAction.setText("New Environment");
		openEnvironmentAction.setToolTipText("New Environment");
		openEnvironmentAction.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages().getImageDescriptor(
						ISharedImages.IMG_OBJ_ELEMENT));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (obj instanceof IndexNode) {
					IndexNode indexNode = (IndexNode) obj;
					openIndexView(indexNode);
				}
			}
		};
	}

	protected void openIndexView(IndexNode indexNode) {
		IWorkbenchPage page = this.getViewSite().getPage();
		try {
			IndexView view = (IndexView) page.showView(IndexView.ID);
			view.selectionChanged(indexNode);
		} catch (PartInitException e) {
			throw new RuntimeException(e);
		}

	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Environment View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}