// This software is released into the Public Domain.  See copying.txt for details.
package org.openstreetmap.osmosis.core.pgsql.v0_6;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.database.DatabaseLoginCredentials;
import org.openstreetmap.osmosis.core.database.DatabasePreferences;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.CopyFilesetLoader;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.NodeLocationStoreType;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.CopyFilesetBuilder;
import org.openstreetmap.osmosis.core.pgsql.v0_6.impl.TempCopyFileset;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;


/**
 * An OSM data sink for storing all data to a database using the COPY command.
 * This task is intended for writing to an empty database.
 * 
 * @author Brett Henderson
 */
public class PostgreSqlCopyWriter implements Sink {
	
	private CopyFilesetBuilder copyFilesetBuilder;
	private CopyFilesetLoader copyFilesetLoader;
	private TempCopyFileset copyFileset;
	
	
	/**
	 * Creates a new instance.
	 * 
	 * @param loginCredentials
	 *            Contains all information required to connect to the database.
	 * @param preferences
	 *            Contains preferences configuring database behaviour.
	 * @param enableBboxBuilder
	 *            If true, the way bbox geometry is built during processing
	 *            instead of relying on the database to build them after import.
	 *            This increases processing but is faster than relying on the
	 *            database.
	 * @param enableLinestringBuilder
	 *            If true, the way linestring geometry is built during
	 *            processing instead of relying on the database to build them
	 *            after import. This increases processing but is faster than
	 *            relying on the database.
	 * @param storeType
	 *            The node location storage type used by the geometry builders.
	 */
	public PostgreSqlCopyWriter(
			DatabaseLoginCredentials loginCredentials, DatabasePreferences preferences,
			boolean enableBboxBuilder, boolean enableLinestringBuilder, NodeLocationStoreType storeType) {
		
		copyFileset = new TempCopyFileset();
		
		copyFilesetBuilder =
			new CopyFilesetBuilder(copyFileset, enableBboxBuilder, enableLinestringBuilder, storeType);
		copyFilesetLoader = new CopyFilesetLoader(loginCredentials, preferences, copyFileset, !enableBboxBuilder,
				!enableLinestringBuilder);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	public void process(EntityContainer entityContainer) {
		copyFilesetBuilder.process(entityContainer);
	}
	
	
	/**
	 * Writes any buffered data to the files, then loads the files into the database. 
	 */
	public void complete() {
		copyFilesetBuilder.complete();
		copyFilesetLoader.run();
	}
	
	
	/**
	 * Releases all database resources.
	 */
	public void release() {
		copyFilesetBuilder.release();
		copyFileset.release();
	}
}