package com.example.locationsfinalproject.control;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.locationsfinalproject.model.LocationsContract;
import com.example.locationsfinalproject.utils.GoogleAccess;

/**
 * a Service for searching a place and saving the results<br>
 * this will perform a search in the background and store the results in the
 * appProvider.<br>
 * (Previous results will be deleted!) <br>
 * <br>
 * start this service with the action: <b>
 * {@code "com.example.locationsfinalproject.control.action.SEARCH"} </b><br>
 * <br>
 * expected extras:<br>
 * <ul>
 * <li>
 * <b>"query"</b> : the search String.</li>
 * </ul>
 */
public class SearchLocationsService extends IntentService {

	public static final String FILTER = "locationservice.resultUpdated";

	private static final String TAG = "SearchLocationsService";
	private Handler mHandler;

	public SearchLocationsService() {
		super("SearchLocationsService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// get the extra from the intent:
		String query = intent.getStringExtra("query");
		String action = intent.getStringExtra("action");
		double latitude = intent.getDoubleExtra("latitude", 0);
		double longitude = intent.getDoubleExtra("longitude", 0);

		if (query == null || query.length() < 2) {
			// it won't search queries less then 2 letters
			return;
		}

		// delete all data from the provider (locations table):
		getContentResolver().delete(LocationsContract.Locations.CONTENT_URI,
				null, null);

		// search:
		// (we're using the GoogleAccess class to do the actual call to the API)
		String netresult = "";
		try {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String radius = prefs.getString("radius", "10000");
			netresult = GoogleAccess.searchLocation(query, action, latitude,
					longitude, radius);
			Log.d(TAG, netresult);

			// the response object:
			JSONObject jsonResult = new JSONObject(netresult);

			// get the "results" array
			JSONArray resultsArray = jsonResult.getJSONArray("results");

			for (int i = 0; i < resultsArray.length(); i++) {
				Log.d(TAG, "result " + i);

				// the current item's object:
				JSONObject result = resultsArray.getJSONObject(i);
				JSONObject geometryObj = result.getJSONObject("geometry");
				JSONObject locationObj = geometryObj.getJSONObject("location");

				// get the relevant fields:
				String address = "";
				if (action.equals("nearbysearch")) {
					address = result.getString("vicinity");
				} else {
					address = result.getString("formatted_address");
				}

				String name = result.getString("name");

				double lat = locationObj.getDouble("lat");
				double lng = locationObj.getDouble("lng");
				String location = lat + ", " + lng;
				double distance = 0;

				String icon = result.getString("icon");
				if (latitude != 0 && longitude != 0) {
					String distancetype = prefs.getString("distancetype", "KM");
					distance = haversine(latitude, longitude, lat, lng,
							distancetype);
				}
				Log.d(TAG, "name:" + name + " index:" + i);
				Log.d(TAG, "address:" + address + " index:" + i);
				Log.d(TAG, "icon:" + icon + " index:" + i);
				Log.d(TAG, "distance:" + distance + "index:" + i);
				Log.d(TAG, "lat:" + lat + " index:" + i);
				Log.d(TAG, "lng:" + lng + " index:" + i);
				Log.d(TAG, "location:" + location + " index:" + i);

				// prepare an ContentValues to insert:
				ContentValues values = new ContentValues();
				values.put(LocationsContract.Locations.NAME, name);
				values.put(LocationsContract.Locations.ADDRESS, address);
				values.put(LocationsContract.Locations.LOCATION, location);
				values.put(LocationsContract.Locations.ICON, icon);
				values.put(LocationsContract.Locations.DISTANCE, distance);
				values.put(LocationsContract.Locations.LATITUDE, lat);
				values.put(LocationsContract.Locations.LONGITUDE, lng);
				values.put(LocationsContract.Locations.LASTQUERY, query);
				values.put(LocationsContract.Locations.LASTACTION, action);

				// insert into the provider (places table)
				getContentResolver().insert(
						LocationsContract.Locations.CONTENT_URI, values);

				// inform bound activity of dataset changes
				Intent intt = new Intent();
				intt.setAction(FILTER);
				sendBroadcast(intt);
			}
		} catch (Exception e) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(
							SearchLocationsService.this,
							"There is no internet connection or an error occurred",
							Toast.LENGTH_LONG).show();
				}
			});
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mHandler = new Handler();
	}

	public static double haversine(double lat1, double lng1, double lat2,
			double lng2, String distancetype) {
		int r = 6371; // average radius of the earth in km
		double dLat = Math.toRadians(lat2 - lat1);
		double dLon = Math.toRadians(lng2 - lng1);
		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.cos(Math.toRadians(lat1))
				* Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
				* Math.sin(dLon / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double d = r * c;

		if (distancetype.equals("MI")) {
			d = (d / 1.61);
		}

		return d;
	}

}
