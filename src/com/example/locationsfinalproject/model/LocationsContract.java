package com.example.locationsfinalproject.model;

import com.example.locationsfinalproject.control.provider.AppProvider;

import android.net.Uri;

/**
 * a contract class for the AppProvider<br>
 * <li>Locations : a table to hold the latest search results</li> </ul>
 * 
 * @see AppProvider
 * @see DbHelper
 * 
 */
public class LocationsContract {

	/** base AUTHORITY for the provider */
	public final static String AUTHORITY = "com.example.locationsfinalproject.control.provider.appProvider";

	/**
	 * Locations table:<br>
	 * hold the latest search results for locations
	 */
	public static class Locations {
		/**
		 * table name
		 * */
		public final static String TABLE_NAME = "locations";

		/**
		 * uri for the locations table<br>
		 * <b>content://com.example.locationsfinalproject.provider/locations</b>
		 */
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		// table columns

		
		public final static String _ID = "_id";

	
		public final static String NAME = "name";

		
		public final static String ADDRESS = "address";

	
		public final static String LOCATION = "location";

		
		public final static String ICON = "icon";

		
		public final static String LATITUDE = "lat";

		
		public final static String LONGITUDE = "lng";
		
	
		public final static String DISTANCE = "distance";
		
		
		public final static String LASTQUERY = "lastquery";
		
		
		public final static String LASTACTION = "lastaction";
	}

	/**
	 * Favorites table:<br>
	 * hold the search results for locations that you wish remember
	 */
	public static class Favorites {
		/**
		 * table name
		 * */
		public final static String TABLE_NAME = "favorites";

		/**
		 * uri for the favorites table<br>
		 * <b>content://com.example.locations.provider/locations</b>
		 */
		public final static Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		// table columns

	
		public final static String _ID = "_id";

	
		public final static String NAME = "name";

		
		public final static String ADDRESS = "address";
		
		
		public final static String LOCATION = "location";
		
		
		public final static String DISTANCE = "distance";
		
	
		public final static String LATITUDE = "lat";

		
		public final static String LONGITUDE = "lng";

	
		public final static String ICON = "icon";
		
		
		public final static String LASTQUERY = "lastquery";
		
		
		public final static String LASTACTION = "lastaction";
	}
}
