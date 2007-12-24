/**
 * 
 */
package org.mindhaus.bdb.browser.entities;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;

/**
 * @author amarch
 *
 */
@Entity
public class MyEntity {

	@PrimaryKey
	protected Long id;
	
	public MyEntity() {
		
	}

	public MyEntity(Long id) {
		this.id = id;
	}
}
