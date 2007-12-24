/**
 * 
 */
package org.mindhaus.bdb.browser.ui.model;

import org.mindhaus.bdb.EnvironmentConnection;

public class EnvironmentNode extends Node {
	private EnvironmentConnection connection;

	public EnvironmentNode(EnvironmentConnection conn) {
		super(conn.getDbHome().getPath());
		this.connection = conn;
	}
	
}