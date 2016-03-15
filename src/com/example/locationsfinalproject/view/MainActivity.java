package com.example.locationsfinalproject.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.locationsfinalproject.R;
import com.example.locationsfinalproject.control.db.myLocation;
import com.example.locationsfinalproject.view.ListFrag.ListFragListener;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends ActionBarActivity implements ListFragListener {

	protected boolean isPhone() {
		View layout = findViewById(R.id.singleFrag);
		if (layout != null) {
			// we are on a phone
			return true;
		} else {
			// not found - we are not.
			return false;
		}
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		// add the list fragment to container
		Fragment listfrag = ListFrag.newInstance();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.add(R.id.container, listfrag, "list");
		ft.commit();

	}

	@Override
	public void onStart() {
		super.onStart();
		// get analytics
		EasyTracker.getInstance(this).activityStart(this); // Add this method.

	}

	@Override
	public void onStop() {
		super.onStop();
		// stop analytics
		EasyTracker.getInstance(this).activityStop(this); // Add this method.

	}

	@Override
	public void onPlaceSelected(myLocation location) {
		FragmentManager fm = getSupportFragmentManager();

		if (isPhone()) {
			// single fragment (phone):
			// we want to replace the fragment with a map fragment

			FragmentTransaction ft = fm.beginTransaction();

			// options for the created map fragment:
			GoogleMapOptions options = new GoogleMapOptions();

			// - map type
			options.mapType(GoogleMap.MAP_TYPE_NORMAL);

			options.compassEnabled(true);
			options.rotateGesturesEnabled(true);
			options.scrollGesturesEnabled(true);
			options.tiltGesturesEnabled(true);
			options.zoomControlsEnabled(true);
			options.zoomGesturesEnabled(true);

			// - initial location - the place
			options.camera(CameraPosition.fromLatLngZoom(
					location.getLocation(), 17));

			// now - create the map fragment with the options:
			// we're using the newInstance(options) method - it's a factory
			// method for creating a mapFragmnet.
			SupportMapFragment mapFragment = SupportMapFragment
					.newInstance(options);

			// add it to the activity:
			ft.setCustomAnimations(R.anim.anim_slide_in_left,
					R.anim.anim_slide_out_left, R.anim.anim_slide_in_right,
					R.anim.anim_slide_out_right);
			ft.replace(R.id.container, mapFragment, "details");
			ft.addToBackStack(null);
			ft.commit();

		} else {
			// dual fragment (tablet):

			// see if the map fragment already exists:
			Fragment testDetails = fm.findFragmentByTag("details");
			if (testDetails == null) {
				// it doesn't.
				// add a new map fragment using factory method :
				// newInstance(options);

				FragmentTransaction ft = fm.beginTransaction();

				// options for the created map fragment:
				GoogleMapOptions options = new GoogleMapOptions();

				// - map type
				options.mapType(GoogleMap.MAP_TYPE_NORMAL);

				options.compassEnabled(true);
				options.rotateGesturesEnabled(true);
				options.scrollGesturesEnabled(true);
				options.tiltGesturesEnabled(true);
				options.zoomControlsEnabled(true);
				options.zoomGesturesEnabled(true);

				// - initial location - the place
				options.camera(CameraPosition.fromLatLngZoom(
						location.getLocation(), 17));

				// creating the map fragment with the options:
				// method for creating a mapFragmnet.
				SupportMapFragment mapFragment = SupportMapFragment
						.newInstance(options);

				// add it to the activity:
				ft.setCustomAnimations(android.R.anim.fade_in,
						android.R.anim.fade_out);

				ft.replace(R.id.detailcontainer, mapFragment, "details");
				ft.commit();
			} else {
				// it does: animate the map:
				// find the fragment:
				SupportMapFragment mapFragment = (SupportMapFragment) fm
						.findFragmentByTag("details");

				// get the map object from the map fragment:
				GoogleMap map = mapFragment.getMap();
				
				// the map camera update:
				CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
						location.getLocation(), 17);
				map.animateCamera(update);
				// put a marker on the position
				Marker marker = map.addMarker(new MarkerOptions().position(location.getLocation()));
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {

		case R.id.action_favorites:
			Intent intent = new Intent(MainActivity.this,
					FavoritesActivity.class);
			startActivity(intent);
			break;
			
		case R.id.action_settings:
			Intent intent2 = new Intent(MainActivity.this,
					SettingsActivity.class);
			startActivity(intent2);
			break;
		}
		return super.onOptionsItemSelected(item);
		
	}
}
