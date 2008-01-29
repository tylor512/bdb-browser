package org.mindhaus.bdb;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import sun.rmi.runtime.GetThreadPoolAction;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.EntityMetadata;
import com.sleepycat.persist.model.EntityModel;
import com.sleepycat.persist.model.PrimaryKeyMetadata;

public class EnvironmentConnection {

	protected final EnvironmentConfig envConfig = new EnvironmentConfig();

	protected final StoreConfig storeConfig = new StoreConfig();

	protected File dbHome;

	protected Environment environment;

	public EnvironmentConnection(File dbHome) {
		this.dbHome = dbHome;
		dbHome.mkdirs();
		envConfig.setTransactional(true);
		envConfig.setAllowCreate(false);
		envConfig.setTxnNoSync(true);
		envConfig.setTxnWriteNoSync(true);

		storeConfig.setAllowCreate(false);
		storeConfig.setTransactional(true);
	}

	public void open() {
		try {
			environment = new Environment(dbHome, envConfig);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			environment.close();
		} catch (DatabaseException e) {
			// eat it
		}
	}

	@Override
	protected void finalize() throws Throwable {
		close();
		super.finalize();
	}

	public File getDbHome() {
		return dbHome;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Set<String> getEntityStores() {
		Set<String> names = new HashSet<String>();
		try {
			names = EntityStore.getStoreNames(environment);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		return names;

	}

	public Set<String> getStoredEntities(String storeName) {
		EntityModel model = getEntityModel(storeName);
		Set<String> classNames = model.getKnownClasses();
		Set<String> storedEntities = new HashSet<String>();
		for (String className : classNames) {
			EntityMetadata metadata = model.getEntityMetadata(className);
			if (metadata != null) {
				storedEntities.add(className);
			}
		}
		return storedEntities;

	}

	public EntityModel getEntityModel(String storeName) {
		EntityStore store = getEntityStore(storeName);
		return store.getModel();
	}

	public EntityStore getEntityStore(String storeName) {
		EntityStore store = null;
		try {
			store = new EntityStore(environment, storeName, storeConfig);
		} catch (DatabaseException e) {
			throw new RuntimeException(e);
		}
		return store;
	}

	public PrimaryIndex getPKIndex(String storeName, String entityName) {
		EntityStore store = getEntityStore(storeName);
		EntityMetadata metadata = store.getModel()
				.getEntityMetadata(entityName);
		PrimaryIndex entitiesById = null;
		if (metadata != null) {
			PrimaryKeyMetadata pkMeta = metadata.getPrimaryKey();
			String pkClassname = pkMeta.getClassName();
			try {
				entitiesById = store.getPrimaryIndex(Class.forName(pkClassname,
						true, Thread.currentThread().getContextClassLoader()),
						Class.forName(entityName, true, Thread.currentThread()
								.getContextClassLoader()));
			} catch (DatabaseException e) {
				throw new RuntimeException(e);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		}
		return entitiesById;
	}

}
