package com.example.locationsfinalproject.control.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.locationsfinalproject.control.provider.AppProvider;
import com.example.locationsfinalproject.model.LocationsContract;




/**
 * a helper class to create / update the database <br>
 * the data itself will be available via the the {@link AppProvider} class<br>
 * @see AppProvider
 * @see PlacesContract
 *
 */
public class DbOpenHelper extends SQLiteOpenHelper {

	/** the current database version */
	private static final int DB_VERSION = 1;
	
	/** the database file name */
	private static final String DB_NAME = "locations.db";

	public DbOpenHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// create the tables:
		String sql = "CREATE TABLE " + LocationsContract.Locations.TABLE_NAME + "("
				+ LocationsContract.Locations._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ LocationsContract.Locations.NAME + " TEXT,"
				+ LocationsContract.Locations.ADDRESS + " TEXT,"
				+ LocationsContract.Locations.LOCATION + " TEXT,"
				+ LocationsContract.Locations.ICON + " TEXT,"
				+ LocationsContract.Locations.DISTANCE + " REAL,"
				+ LocationsContract.Locations.LATITUDE + " REAL,"
				+ LocationsContract.Locations.LONGITUDE + " REAL,"
				+ LocationsContract.Locations.LASTQUERY + " TEXT,"
				+ LocationsContract.Locations.LASTACTION + " TEXT" + ");";
		db.execSQL(sql);
	
		String sql2 = "CREATE TABLE " + LocationsContract.Favorites.TABLE_NAME + "("
				+ LocationsContract.Locations._ID
				+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ LocationsContract.Locations.NAME + " TEXT,"
				+ LocationsContract.Locations.ADDRESS + " TEXT,"
				+ LocationsContract.Locations.LOCATION + " TEXT,"
				+ LocationsContract.Locations.ICON + " TEXT,"
				+ LocationsContract.Locations.DISTANCE + " REAL,"
				+ LocationsContract.Locations.LATITUDE + " REAL,"
				+ LocationsContract.Locations.LONGITUDE + " REAL,"
				+ LocationsContract.Locations.LASTQUERY + " TEXT,"
				+ LocationsContract.Locations.LASTACTION + " TEXT" + ");";
		db.execSQL(sql2);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// delete the tables:
		String sql = "DROP TABLE IF EXISTS " + LocationsContract.Locations.TABLE_NAME;
		db.execSQL(sql);

		String sql2 = "DROP TABLE IF EXISTS " + LocationsContract.Favorites.TABLE_NAME;
		db.execSQL(sql2);
		//recreate the tables:
		onCreate(db);
	}

}
