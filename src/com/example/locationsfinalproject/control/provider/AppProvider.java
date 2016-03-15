package com.example.locationsfinalproject.control.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.example.locationsfinalproject.control.db.DbOpenHelper;
import com.example.locationsfinalproject.model.LocationsContract;

/**
 * the content provider for the application's data
 * 
 * @see LocationsContract
 */

public class AppProvider extends ContentProvider {
	private DbOpenHelper dbHelper;

	@Override
	public boolean onCreate() {
		// create the dbHelper:
		dbHelper = new DbOpenHelper(getContext());
		if (dbHelper != null) {
			// success
			return false;
		} else {
			// fail
			return true;
		}
	}

	/**
	 * helper method // get the first part of the uri path - this is the table
	 * name // example: // content://com.example.places.provider/locations
	 */
	protected String getTableName(Uri uri) {
		List<String> pathSegments = uri.getPathSegments();
		return pathSegments.get(0);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		// in order to get a readable db :
		SQLiteDatabase db = dbHelper.getReadableDatabase();

		// in order to do the query:
		Cursor cursor = db.query(getTableName(uri), projection, selection,
				selectionArgs, null, null, sortOrder);

		// register the cursor to track changes on the uri
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		// in order to get a writable database:
		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// do the insert:
		long id = db.insertWithOnConflict(getTableName(uri), null, values,
				SQLiteDatabase.CONFLICT_REPLACE);

		// notify the change
		getContext().getContentResolver().notifyChange(uri, null);

		if (id > 0) {
			// return a uri to the inserted row:
			// ( i.e content://com.example.locations.provider/locations/5 )
			return ContentUris.withAppendedId(uri, id);
		} else {
			// return null if non inserted
			return null;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// do the update
		int result = db.update(getTableName(uri), values, selection,
				selectionArgs);

		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		SQLiteDatabase db = dbHelper.getWritableDatabase();

		// deleting:
		int result = db.delete(getTableName(uri), selection, selectionArgs);

		// notify the change
		getContext().getContentResolver().notifyChange(uri, null);
		return result;
	}
	
}
