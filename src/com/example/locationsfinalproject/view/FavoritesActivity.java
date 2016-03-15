package com.example.locationsfinalproject.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import com.example.locationsfinalproject.R;
import com.example.locationsfinalproject.control.db.myLocation;
import com.example.locationsfinalproject.model.LocationsContract;

import com.google.android.gms.maps.model.LatLng;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;

public class FavoritesActivity extends ActionBarActivity implements LoaderCallbacks<Cursor>, LocationListener {
	private static final int FAVORITES_LOADER_ID = 1;
	private FavoritesAdapter adapter;
	public String provider;
	public LocationManager locationManager;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorites);

		ListView listView = (ListView) findViewById(R.id.favoriteslist);
		// create the adapter - we'll later fill the data using a cursor loader
		adapter = new FavoritesAdapter(this, null);

		// connect list and adapter:
		listView.setAdapter(adapter);

		// register the list with action bar:
		registerForContextMenu(listView);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// start a loader -
		// this will fetch the data in the background
		getSupportLoaderManager().initLoader(FAVORITES_LOADER_ID, null, this);

		// a handler to the UI thread
		final Handler handler = new Handler();
		// do something on the UI thread in 100 milliseconds
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// try to get a location from provider:
				getMyLatLng();

				if (getMyLatLng() == null) {
					// still no location... retry in a short while
					handler.postDelayed(this, 100);
				}
			}
		}, 100);
	}

	// --- LoaderCallbacks implementation:
	// this will initialize the loader and request a query
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

		// the Uri for calls log
		Uri uri = LocationsContract.Favorites.CONTENT_URI;

		// these are the columns we want to get from the provider
		String[] projection = { LocationsContract.Favorites._ID,
				LocationsContract.Favorites.NAME,
				LocationsContract.Favorites.ADDRESS,
				LocationsContract.Favorites.LOCATION,
				LocationsContract.Favorites.ICON,
				LocationsContract.Favorites.DISTANCE,
				LocationsContract.Favorites.LATITUDE,
				LocationsContract.Favorites.LONGITUDE,
				LocationsContract.Favorites.LASTQUERY,
				LocationsContract.Favorites.LASTACTION };

		return new CursorLoader(this, uri, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// call swapCursor() instead of changeCursor()
		adapter.swapCursor(cursor);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// reset - don't use the cursor anymore
		adapter.swapCursor(null);

	}

	  
	 /////////////////////////////// THE ADAPTER ////////////////////////////////////
	 
	class FavoritesAdapter extends CursorAdapter {

		public FavoritesAdapter(Context context, Cursor c) {
			super(context, c, 0);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// inflate the view from XML
			return LayoutInflater.from(context).inflate(R.layout.item_list,
					parent, false);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			// get the data from cursor:
			long id = cursor.getLong(cursor
					.getColumnIndex(LocationsContract.Favorites._ID));
			String name = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites.NAME));
			String address = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites.ADDRESS));
			String icon = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites.ICON));
			double distance = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Favorites.DISTANCE));
			double latitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Favorites.LATITUDE));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Favorites.LONGITUDE));
			String lastquery = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites.LASTQUERY));
			String lastaction = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites.LASTACTION));
			LatLng location = new LatLng(latitude, longitude);

			myLocation result = new myLocation(id, name, address, location,
					icon, distance, latitude, longitude, lastquery, lastaction);

			// get the sub-views in the item's layout:
			TextView tvName = (TextView) view.findViewById(R.id.name);
			TextView tvAddress = (TextView) view.findViewById(R.id.address);
			TextView tvDistance = (TextView) view.findViewById(R.id.distance);
			ImageView imageThumb = (ImageView) view.findViewById(R.id.icon);

			// bind the data

			// name:
			tvName.setText(name);

			// address:
			tvAddress.setText(address);

			// distance:
			if (distance != 0) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(FavoritesActivity.this);
				String distancetype = prefs.getString("distancetype", "KM");
				distance = Math.floor(distance * 100) / 100;

				if (distancetype.equals("KM")) {
					tvDistance.setText(new DecimalFormat("#######.##")
							.format(distance * 1.61f) + " KM");
				} else if (distancetype.equals("MI")) {
					tvDistance.setText(new DecimalFormat("#######.##")
							.format(distance) + " MI");
				} else {
					tvDistance.setText("");
				}

				// handle the image:
				// it needs to be downloaded in a task.

				// first - show the progress bar, show default image:
				imageThumb.setImageResource(R.drawable.location_icon);

				// set the movie as the view's tag
				// this will identify the view later.
				view.setTag(result);

				// then - start downloading the image (task):
				new GetImageTask(view, result).execute();

			}

		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
	
	@Override
	public void onPause() {
		super.onPause();

		// stop location updates (to save battery)
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(android.location.Location location) {
		Cursor cursor = getContentResolver()
				.query(LocationsContract.Favorites.CONTENT_URI, null, null,
						null, null);

		cursor.moveToFirst();
		String id = "";
		double latitude = 0;
		double longitude = 0;
		double newDistance = 0;
		double newLat = location.getLatitude();
		double newLng = location.getLongitude();

		while (cursor.moveToNext()) {

			// get the id and lat&lng from the results
			id = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Favorites._ID));
			latitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Favorites.LATITUDE));
			longitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Favorites.LONGITUDE));

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String distancetype = prefs.getString("distancetype", "KM");
			newDistance = haversine(newLat, newLng, latitude, longitude,
					distancetype);

			ContentValues values = new ContentValues();
			values.put(LocationsContract.Favorites.DISTANCE, newDistance);

			// update into the provider (results table)
			getContentResolver()
					.update(LocationsContract.Favorites.CONTENT_URI, values,
							LocationsContract.Favorites._ID + "=?",
							new String[] { id });
			values.clear();
		}

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
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

	private Location getMyLatLng() {

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// get the best location-provider that matches a certain
		// criteria:
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		provider = locationManager.getBestProvider(criteria, true);

		locationManager.requestLocationUpdates(provider, 0, 100, this);

		// try to get the last known location with the given provider
		Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

		return lastKnownLocation;
	}
}

// GetImageTask
// task for downloading images and putting them in the list item.
class GetImageTask extends AsyncTask<Void, Void, Bitmap> {

	private View v;
	private myLocation location;

	public GetImageTask(View v, myLocation location) {
		this.v = v;
		this.location = location;
	}

	@Override
	protected Bitmap doInBackground(Void... params) {
		// get the image url
		String address = location.getIcon();

		HttpURLConnection connection = null;
		InputStream stream = null;
		ByteArrayOutputStream outputStream = null;

		// the bitmap will go here:
		Bitmap b = null;

		try {
			// build the URL:
			URL url = new URL(address);
			// open a connection:
			connection = (HttpURLConnection) url.openConnection();

			// check the connection response code:
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				// not good..
				return null;
			}

			// the input stream:
			stream = connection.getInputStream();

			// a stream to hold the read bytes.
			// (like the StringBuilder we used before)
			outputStream = new ByteArrayOutputStream();

			// a byte buffer for reading the stream in 1024 bytes chunks:
			byte[] buffer = new byte[1024];

			int bytesRead = 0;

			// read the bytes from the stream
			while ((bytesRead = stream.read(buffer, 0, buffer.length)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			// flush the output stream - write all the pending bytes in its
			// internal buffer.
			outputStream.flush();

			// get a byte array out of the outputStream
			// theses are the bitmap bytes
			byte[] imageBytes = outputStream.toByteArray();

			// use the BitmapFactory to convert it to a bitmap
			b = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				// close connection:
				connection.disconnect();
			}
			if (outputStream != null) {
				try {
					// close output stream:
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return b;
	}

	@Override
	protected void onPostExecute(Bitmap result) {

		// ok we got the bitmap.
		// but during the time it took -
		// the view in the listView might have been recycled...
		// we don't want to display the image if the view is recycled
		// check if the view (the list item) has the same tag
		// if it's not - don't put the bitmap there

		Object tag = v.getTag();
		if (tag != null && tag.equals(location)) {
			// put the bitmap in it.

			// the sub views
			ImageView imageThumb = (ImageView) v.findViewById(R.id.icon);

			// display image (if it's null - display a default image).
			if (result != null) {
				imageThumb.setImageBitmap(result);
			} else {
				imageThumb.setImageResource(R.drawable.location_icon);
			}
		}
	}
	
}
