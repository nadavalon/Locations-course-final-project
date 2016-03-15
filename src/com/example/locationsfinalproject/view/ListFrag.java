package com.example.locationsfinalproject.view;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.location.Location;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.example.locationsfinalproject.R;
import com.example.locationsfinalproject.control.SearchLocationsService;
import com.example.locationsfinalproject.control.db.myLocation;
import com.example.locationsfinalproject.model.LocationsContract;
import com.google.android.gms.maps.model.LatLng;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class ListFrag extends Fragment implements OnClickListener,
		OnScrollListener, LocationListener, OnItemClickListener,
		LoaderCallbacks<Cursor>, OnEditorActionListener,
		OnRefreshListener<ListView> {

	private ListFragListener listener;
	private static final int LOCATIONS_LOADER_ID = 1;
	private PullToRefreshListView listView;
	private LocationsAdapter adapter;
	private EditText search;
	public LocationManager locationManager;
	public String provider;
	private Location myLastLocation;

	private IntentFilter filter = new IntentFilter(
			SearchLocationsService.FILTER);
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// service inform that dataset has changed and should refresh the
			// view
			if (adapter != null)
				adapter.notifyDataSetChanged();
		}

	};

	/**
	 * this is the interface for this fragment we will use this interface to
	 * send events to the activity
	 */
	public interface ListFragListener {
		void onPlaceSelected(myLocation location);
	}

	// new instance of this fragment.
	public static ListFrag newInstance() {

		// create a fragment:
		ListFrag listfrag = new ListFrag();

		// return the prepared fragment:
		return listfrag;
	}

	// -- when the fragment attach to the activity
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			listener = (ListFragListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("activity " + activity.toString()
					+ "must implement ListFragmentListener!");
		}

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// inflate the view from XML :
		View view = inflater.inflate(R.layout.listfrag, container, false);

		// the list view:
		listView = (PullToRefreshListView) view
				.findViewById(R.id.pull_to_refresh_listview);

		// button clicks and editText search listeners:
		search = (EditText) view.findViewById(R.id.etsearch);

		Button searchbyname = (Button) view.findViewById(R.id.searchbyname);
		Button searchnearby = (Button) view.findViewById(R.id.searchnearby);

		// create the adapter:
		adapter = new LocationsAdapter(getActivity(), null);

		// register the list with action bar:
		registerForContextMenu(listView.getRefreshableView());

		// connect list and adapter:
		listView.setAdapter(adapter);

		// item - click:
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
		listView.setOnRefreshListener(this);
		searchbyname.setOnClickListener(this);
		searchnearby.setOnClickListener(this);
		search.setOnEditorActionListener(this);

		// init a cursor loader:
		getActivity().getSupportLoaderManager().initLoader(LOCATIONS_LOADER_ID,
				null, this);

		if (isNetworkAvailable() == true) {

			// a handler to the UI thread
			final Handler handler = new Handler();
			// do something on the UI thread in 100 milliseconds
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// try to get a location from provider:
					myLastLocation = getLatLng();
					if (myLastLocation != null) {
						// if location
						doRefresh();
					}
				}
			}, 100);

		} else {
			Toast.makeText(getActivity(),
					"There is no internet connection or an error occurred",
					Toast.LENGTH_SHORT).show();
		}

		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		getActivity().registerReceiver(mReceiver, filter);
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getActivity().unregisterReceiver(mReceiver);
	}

	// this is the pull to refresh listener:
	@Override
	public void onRefresh(PullToRefreshBase<ListView> refreshView) {
		if (isNetworkAvailable() == true) {
			doRefresh();
		} else {
			// a handler to the UI thread
			final Handler handler = new Handler();
			// do something on the UI thread in 100 milliseconds
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					// try to close refresh:
					listView.onRefreshComplete();
					if (listView.isRefreshing()) {
						// still refreshing... retry in a short while
						handler.postDelayed(this, 100);
					}
				}
			}, 100);
			Toast.makeText(getActivity(),
					"There is no internet connection or an error occurred",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> list, View itemview, int pos, long id) {
		// hide the keyboard
		hideSoftKeyboard();

		int position = (pos - 1);
		// get the clicked place (from the cursor):
		String sortOrder = LocationsContract.Locations.DISTANCE + " asc";

		Cursor cursor = getActivity().getContentResolver().query(
				LocationsContract.Locations.CONTENT_URI, null, null, null,
				sortOrder);
		cursor.moveToPosition((position));
		String name = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.NAME));
		String address = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.ADDRESS));
		String icon = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.ICON));
		double distance = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.DISTANCE));
		double latitude = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.LATITUDE));
		double longitude = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.LONGITUDE));
		String lastquery = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.LASTQUERY));
		String lastaction = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.LASTACTION));
		LatLng location = new LatLng(latitude, longitude);

		myLocation result = new myLocation(id, name, address, location, icon,
				distance, latitude, longitude, lastquery, lastaction);

		// send the city up to the activity:
		listener.onPlaceSelected(result);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// super.onCreateContextMenu(menu, v, menuInfo);
		getActivity().getMenuInflater().inflate(R.menu.menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		int pos = info.position;

		int position = (pos - 1);

		// hide the keyboard
		hideSoftKeyboard();

		// get the clicked place (from the cursor):
		String sortOrder = LocationsContract.Locations.DISTANCE + " asc";

		Cursor cursor = getActivity().getContentResolver().query(
				LocationsContract.Locations.CONTENT_URI, null, null, null,
				sortOrder);
		cursor.moveToPosition((position));
		String name = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.NAME));
		String address = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.ADDRESS));
		String icon = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.ICON));
		double distance = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.DISTANCE));
		double latitude = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.LATITUDE));
		double longitude = cursor.getDouble(cursor
				.getColumnIndex(LocationsContract.Locations.LONGITUDE));
		String lastquery = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.LASTQUERY));
		String lastaction = cursor.getString(cursor
				.getColumnIndex(LocationsContract.Locations.LASTACTION));
		String location = latitude + ", " + longitude;

		switch (item.getItemId()) {
		case R.id.action_addtofavorites:

			// prepare an ContentValues to insert:
			ContentValues values = new ContentValues();
			values.put(LocationsContract.Favorites.NAME, name);
			values.put(LocationsContract.Favorites.ADDRESS, address);
			values.put(LocationsContract.Favorites.LOCATION, location);
			values.put(LocationsContract.Favorites.ICON, icon);
			values.put(LocationsContract.Favorites.DISTANCE, distance);
			values.put(LocationsContract.Favorites.LATITUDE, latitude);
			values.put(LocationsContract.Favorites.LONGITUDE, longitude);
			values.put(LocationsContract.Favorites.LASTQUERY, lastquery);
			values.put(LocationsContract.Favorites.LASTACTION, lastaction);

			// insert into the provider (results table)
			getActivity().getContentResolver().insert(
					LocationsContract.Favorites.CONTENT_URI, values);

			Toast.makeText(getActivity(),
					"The location you chose has been added to favorites",
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.action_share:
			Intent sharingIntent = new Intent(
					android.content.Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT,
					"I recommend this place: " + name);
			sharingIntent.putExtra(Intent.EXTRA_TEXT, address);

			startActivity(Intent.createChooser(sharingIntent, "Share \"" + name
					+ "\" details"));
			break;
		}
		return super.onContextItemSelected(item);
	}

	// listeners:
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.searchbyname:
			try {

				if (isNetworkAvailable() == true) {

					// a handler to the UI thread
					final Handler handler = new Handler();
					// do something on the UI thread in 100 milliseconds
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// try to get a location from provider:
							if (myLastLocation == null) {
								myLastLocation = getLatLng();
								Toast.makeText(
										getActivity(),
										"Please wait while your location is being found.."+ "\n" +"please ensure that a location provider is enabled",
										Toast.LENGTH_LONG).show();
							} else {
								double lat = myLastLocation.getLatitude();
								double lng = myLastLocation.getLongitude();
								System.out.println("textsearch" + "" + lat + ""
										+ lng);
								doSearch(null, "textsearch", lat, lng);
							}
						}
					}, 100);

				} else {
					Toast.makeText(
							getActivity(),
							"There is no internet connection or an error occurred",
							Toast.LENGTH_LONG).show();
				}

			} catch (Exception e) {
				Toast.makeText(
						getActivity(),
						"Please enable the GPS provider to get a distance from the location",
						Toast.LENGTH_LONG).show();
				doSearch(null, "textsearch", 0, 0);

			}
			break;
		case R.id.searchnearby:

			try {
				if (isNetworkAvailable() == true) {

					// a handler to the UI thread
					final Handler handler = new Handler();
					// do something on the UI thread in 100 milliseconds
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// try to get a location from provider:

							if (myLastLocation == null) {
								myLastLocation = getLatLng();
								Toast.makeText(
										getActivity(),
										"Trying to get your nearby location, please ensure that a location provider is enabled",
										Toast.LENGTH_LONG).show();
							} else {
								double lat = myLastLocation.getLatitude();
								double lng = myLastLocation.getLongitude();
								doSearch(null, "nearbysearch", lat, lng);
							}
						}
					}, 100);

				} else {
					Toast.makeText(
							getActivity(),
							"There is no internet connection or an error occurred",
							Toast.LENGTH_LONG).show();
				}

			} catch (Exception e) {
				Toast.makeText(getActivity(), "Please enable the GPS provider",
						Toast.LENGTH_LONG).show();

			}
			break;
		}
	}

	// editText listener:
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		int id = v.getId();
		if (id == R.id.etsearch) {
			try {

				if (isNetworkAvailable() == true) {

					// a handler to the UI thread
					final Handler handler = new Handler();
					// do something on the UI thread in 100 milliseconds
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							// try to get a location from provider:
							getLatLng();
							Toast.makeText(
									getActivity(),
									"Trying to get your location, please ensure that a location provider is enabled",
									Toast.LENGTH_LONG).show();
							if (getLatLng() == null) {
								// still no location... retry in a short while
								handler.postDelayed(this, 100);

							} else {
								Location Location = getLatLng();
								double lat = Location.getLatitude();
								double lng = Location.getLongitude();
								doSearch(null, "textsearch", lat, lng);
							}
						}
					}, 100);

				} else {
					Toast.makeText(
							getActivity(),
							"There is no internet connection or an error occurred",
							Toast.LENGTH_LONG).show();
				}

			} catch (Exception e) {
				Toast.makeText(
						getActivity(),
						"Please enable the GPS provider to get a distance from the location",
						Toast.LENGTH_LONG).show();
				doSearch(null, "textsearch", 0, 0);

			}
			return true;
		}
		return false;
	}

	/**
	 * helper method<br>
	 * this will start the search service and give it the query from the
	 * editText<br>
	 * (will verify 2+ letters)
	 */
	private void doSearch(String query, String action, double latitude,
			double longitude) {
		EditText search = (EditText) getActivity().findViewById(R.id.etsearch);
		query = search.getText().toString();

		if (query.length() > 1) {

			// call service with extra
			Intent intent = new Intent(
					"com.example.locationsfinalproject.control.action.SEARCH");
			intent.setPackage("com.example.locationsfinalproject");
			intent.putExtra("query", query);
			intent.putExtra("action", action);
			intent.putExtra("latitude", latitude);
			intent.putExtra("longitude", longitude);

			getActivity().startService(intent);
		} else {
			Toast.makeText(getActivity(), "enter at least 2 letters...",
					Toast.LENGTH_SHORT).show();
		}

	}

	// initialize the loader and request a query
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {

		// the Uri for calls log
		// this is the "address" of the provider providing our data
		Uri uri = LocationsContract.Locations.CONTENT_URI;

		// all columns
		String[] projection = { LocationsContract.Locations._ID,
				LocationsContract.Locations.NAME,
				LocationsContract.Locations.ADDRESS,
				LocationsContract.Locations.LOCATION,
				LocationsContract.Locations.ICON,
				LocationsContract.Locations.DISTANCE,
				LocationsContract.Locations.LATITUDE,
				LocationsContract.Locations.LONGITUDE,
				LocationsContract.Locations.LASTQUERY,
				LocationsContract.Locations.LASTACTION };

		// sort by name
		String sortOrder = LocationsContract.Locations.DISTANCE + " asc";

		// create the cursor loader:
		// the loader contains the query and it will later run in the background
		// by android.
		// the result cursor will be given to us in the method onLoadFinished()
		return new CursorLoader(getActivity(), uri, projection, null, null,
				sortOrder);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		// cursor in ready
		// swap it into the adapter:
		adapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// cursor is going to be reset
		// remove it from the adapter:
		adapter.swapCursor(null);
	}

	// //////////////////////////////////////////////////
	// THE ADAPTER //
	// /////////////////////////////////////////////////
	class LocationsAdapter extends CursorAdapter {

		public LocationsAdapter(Context context, Cursor c) {
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
					.getColumnIndex(LocationsContract.Locations._ID));
			String name = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.NAME));
			String address = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.ADDRESS));
			String icon = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.ICON));
			double distance = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Locations.DISTANCE));
			double latitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Locations.LATITUDE));
			double longitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Locations.LONGITUDE));
			String lastquery = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.LASTQUERY));
			String lastaction = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.LASTACTION));
			LatLng location = new LatLng(latitude, longitude);

			myLocation result = new myLocation(id, name, address, location,
					icon, distance, latitude, longitude, lastquery, lastaction);

			// get the sub-views in the item's layout:
			TextView tvName = (TextView) view.findViewById(R.id.name);
			TextView tvAddress = (TextView) view.findViewById(R.id.address);
			TextView tvDistance = (TextView) view.findViewById(R.id.distance);
			ImageView imageThumb = (ImageView) view.findViewById(R.id.icon);

			// bind data to views:

			// name:
			tvName.setText(name);

			// address:
			tvAddress.setText(address);

			// distance:
			if (distance != 0) {
				SharedPreferences prefs = PreferenceManager
						.getDefaultSharedPreferences(getActivity());
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
				// it needs to be downloaded in a task...

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

	@Override
	public void onPause() {
		super.onPause();

		// when activity is invisible -
		// stop location updates (to save battery)
		if (locationManager != null) {
			locationManager.removeUpdates(this);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		String sortOrder = LocationsContract.Locations.DISTANCE + " asc";

		Cursor cursor = getActivity().getContentResolver().query(
				LocationsContract.Locations.CONTENT_URI, null, null, null,
				sortOrder);

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
					.getColumnIndex(LocationsContract.Locations._ID));
			latitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Locations.LATITUDE));
			longitude = cursor.getDouble(cursor
					.getColumnIndex(LocationsContract.Locations.LONGITUDE));

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getActivity());
			String distancetype = prefs.getString("distancetype", "KM");
			newDistance = haversine(newLat, newLng, latitude, longitude,
					distancetype);

			ContentValues values = new ContentValues();
			values.put(LocationsContract.Locations.DISTANCE, newDistance);

			// update into the provider (results table)
			getActivity().getContentResolver()
					.update(LocationsContract.Locations.CONTENT_URI, values,
							LocationsContract.Locations._ID + "=?",
							new String[] { id });
			values.clear();

			myLastLocation = location;
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

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		hideSoftKeyboard();
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		// TODO Auto-generated method stub

	}

	private Location getLatLng() {
		// set up location:
		// get an instance of the location service
		try {
			locationManager = (LocationManager) getActivity().getSystemService(
					Context.LOCATION_SERVICE);
		} catch (Exception e) {
			// TODO: handle exception
		}
		// get the best location-provider that matches a certain
		// criteria:
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		if (locationManager != null) {
			provider = locationManager.getBestProvider(criteria, true);
			if (provider != null) {
				locationManager.requestLocationUpdates(provider, 0, 100, this);
				// try to get the last known location with the given provider
				Location lastKnownLocation = locationManager
						.getLastKnownLocation(provider);
				return lastKnownLocation;
			}
		}

		return null;
	}

	private void doRefresh() {
		listView.setRefreshing();
		try {

			String sortOrder = LocationsContract.Locations.DISTANCE + " asc";
			Cursor cursor = getActivity().getContentResolver().query(
					LocationsContract.Locations.CONTENT_URI, null, null, null,
					sortOrder);
			cursor.moveToFirst();

			String lastquery = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.LASTQUERY));
			String lastaction = cursor.getString(cursor
					.getColumnIndex(LocationsContract.Locations.LASTACTION));

			if (myLastLocation == null)
				myLastLocation = getLatLng();
			double lat = myLastLocation.getLatitude();
			double lng = myLastLocation.getLongitude();

			doSearch(lastquery, lastaction, lat, lng);

		} catch (Exception e) {
			try {
				String sortOrder = LocationsContract.Locations.DISTANCE
						+ " asc";
				Cursor cursor = getActivity().getContentResolver().query(
						LocationsContract.Locations.CONTENT_URI, null, null,
						null, sortOrder);
				cursor.moveToFirst();

				String lastquery = cursor.getString(cursor
						.getColumnIndex(LocationsContract.Locations.LASTQUERY));
				String lastaction = cursor
						.getString(cursor
								.getColumnIndex(LocationsContract.Locations.LASTACTION));

				Toast.makeText(
						getActivity(),
						"Please enable the GPS provider to get a distance from the location",
						Toast.LENGTH_LONG).show();
				doSearch(lastquery, lastaction, 0, 0);
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
		// a handler to the UI thread
		final Handler handler = new Handler();
		// do something on the UI thread in 100 milliseconds
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				// try to close refresh:
				listView.onRefreshComplete();
				if (listView.isRefreshing()) {
					// still refreshing... retry in a short while
					handler.postDelayed(this, 100);
				}
			}
		}, 100);

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

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private void hideSoftKeyboard() {
		if (getActivity().getCurrentFocus() != null
				&& getActivity().getCurrentFocus() instanceof EditText) {
			InputMethodManager imm = (InputMethodManager) getActivity()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
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
				b = BitmapFactory.decodeByteArray(imageBytes, 0,
						imageBytes.length);

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
				// ok it's thae same movie.
				// put the bitmap in it.

				// the sub views
				ImageView imageThumb = (ImageView) v.findViewById(R.id.icon);

				// dusplay image (if it's null - display a default image).
				if (result != null) {
					imageThumb.setImageBitmap(result);
				} else {
					imageThumb.setImageResource(R.drawable.location_icon);
				}
			}

		}
	}
}
