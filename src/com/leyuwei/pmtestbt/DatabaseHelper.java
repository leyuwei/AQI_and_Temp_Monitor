package com.leyuwei.pmtestbt;

import java.sql.SQLException;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
	
	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "pmtester.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 13;
	// the DAO object we use to access the SimpleData table
	private Dao<AQIData, Date> dataDao = null;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void dropAllTables(){
		try {
			TableUtils.dropTable(connectionSource, AQIData.class, true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			TableUtils.createTable(connectionSource, AQIData.class);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion,
			int newVersion) {
		try {
			TableUtils.dropTable(connectionSource, AQIData.class, true);
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Returns the Database Access Object (DAO) for our SimpleData class. It will create it or just give the cached
	 * value.
	 */
	public Dao<AQIData, Date> getDataDao() throws SQLException {
		if (dataDao == null) {
			dataDao = getDao(AQIData.class);
		}
		return dataDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		dataDao = null;
	}


}
