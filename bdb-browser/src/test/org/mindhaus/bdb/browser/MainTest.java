/**
 * 
 */
package org.mindhaus.bdb.browser;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.sleepycat.bind.tuple.MarshalledTupleEntry;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleMarshalledBinding;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.CursorConfig;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * @author amarch
 *
 */
public class MainTest {
	
	Logger log = Logger.getAnonymousLogger();

    private Environment environment;

    private File dbHome;
    private DatabaseConfig dbConfig;

    Database db;
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeTest()
	public void setUp() throws Exception {
		final EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);
        envConfig.setTxnNoSync(true);
        envConfig.setTxnWriteNoSync(true);

        dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        dbHome = new File("target/test/db");
        dbHome.mkdirs();
        this.environment = new Environment(this.dbHome, envConfig);
        db = this.environment.openDatabase(null, "testDb" ,
                dbConfig);
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();
        StringBinding.stringToEntry("aKey", keyEntry);
        StringBinding.stringToEntry("aValue", dataEntry);
        db.put(null, keyEntry, dataEntry);
	}
	
	@AfterTest
	public void cleanUp() {
		try {
			this.db.close();
			this.environment.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test()
	public void testEnvironment() {
		Cursor cursor = null;
		try {
			List dbNames = this.environment.getDatabaseNames();
			log.log(Level.INFO, "DB Names : " + dbNames);
			CursorConfig cc = new CursorConfig();
			cc.setReadUncommitted(true);
			cursor = db.openCursor(null, cc);
			DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();

            while (cursor.getNext(foundKey, foundData,
                LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS) {

                if (foundData.getData() != null) {
//                	TupleInput input = new TupleInput(foundData.getData());
                	TupleMarshalledBinding binding = new TupleMarshalledBinding(MarshalledTupleEntry.class);
                	Object data = binding.entryToObject(foundData);
                	String key = StringBinding.entryToString(foundKey);
                	//String data = StringBinding.entryToString(foundData);
                	log.log(Level.INFO, "DB Entry : key=" + key + ",data=" + data);
        			
                }
            }
            log.log(Level.INFO, "DB Size : " + cursor.count());
            
		} catch (Exception e) {
			throw new RuntimeException(e);			
		} finally {
			try {
				cursor.close();
			} catch (DatabaseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	

}
