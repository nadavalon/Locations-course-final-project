package com.example.locationsfinalproject.view;

import com.example.locationsfinalproject.R;
import com.example.locationsfinalproject.model.LocationsContract;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class SettingsActivity extends ActionBarActivity implements OnClickListener,OnCheckedChangeListener{

	private DialogInterface.OnClickListener dialogClickListener;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		Button deletefavorites = (Button) findViewById(R.id.deleteall);
		deletefavorites.setOnClickListener(this);
		RadioGroup uomRadioGroup = (RadioGroup) findViewById(R.id.distancetype);
		
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String uom = pref.getString("distancetype", "KM");
		
		if(uom.equals("KM")){
			uomRadioGroup.check(R.id.kilometers);
		} else {
			uomRadioGroup.check(R.id.miles);
		}
		
		uomRadioGroup.setOnCheckedChangeListener((OnCheckedChangeListener) this);

		dialogClickListener = new DialogInterface.OnClickListener() {
				
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:

					getContentResolver()
							.delete(LocationsContract.Favorites.CONTENT_URI,
									null, null);

					Toast.makeText(SettingsActivity.this, "All items deleted",
							Toast.LENGTH_SHORT).show();
					break;

				case DialogInterface.BUTTON_NEGATIVE:
					dialog.dismiss();
					break;
				}
			}
		};
	}
	
	@Override
	public void onClick(View v) {
		Button deletefavorites = (Button) findViewById(R.id.deleteall);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete all Favorites?")
		.setPositiveButton("Yes", dialogClickListener)
		.setNegativeButton("No", dialogClickListener).show();

	}

	////////// Handle the options///////////////////////
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_prefs, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		switch (id) {

		case R.id.action_favorites:
			Intent intent = new Intent(SettingsActivity.this,
					FavoritesActivity.class);
			startActivity(intent);
			break;
			
		
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(SettingsActivity.this, MainActivity.class);
		startActivity(intent);
		finish();

	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		
		switch(group.getCheckedRadioButtonId()){
		case R.id.kilometers:
			editor.putString("distancetype", "KM");
			break;
		case R.id.miles:
			editor.putString("distancetype", "MI");
			break;
		}
		
		editor.commit();
	}



}
