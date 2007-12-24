package org.mindhaus.bdb.browser.ui.model;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityIndex;

public class IndexNode extends Node {

	private EntityIndex index;

	public IndexNode(EntityIndex index) {
		super("Index <" + index + ">");
		this.index = index;
		
	}

	public Iterable getEntries() {
		try {
			return index.entities();
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

}
