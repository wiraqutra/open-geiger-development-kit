package org.fukushima.OpenGeiger.Hand;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.fukushima.OpenGeiger.LocationAPI;
import org.fukushima.OpenGeiger.LocationAPIListener;
import org.fukushima.OpenGeiger.R;
import org.fukushima.OpenGeiger.WebAPI;
import org.fukushima.OpenGeiger.WebAPIListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

public class HandClient extends MapActivity implements WebAPIListener, LocationAPIListener {

	/**
	 * Tag
	 */
	private static final String TAG = "HAND_CLIENT";

	/**
	 * Spinner for Device
	 */
	private Spinner spinnerDevice;

	/**
	 * TextBot for Input
	 */
	private EditText editText;

	/**
	 * TextBot for Input
	 */
	private EditText editLat;

	/**
	 * TextBot for Input
	 */
	private EditText editLon;

	/**
	 * Context
	 */
	private Context mContext;

	/**
	 * Application
	 */
	private Application mApplication;

	/**
	 * MapView
	 */
	private MapView mMap;

	/**
	 * MapController
	 */
	private MapController mMapController;

	/**
	 * Pin
	 */
	private Drawable mPin;

	/**
	 * Pin Overlay
	 */
	private PinOverlay mOverlay;

	/**
	 * Lat
	 */
	private double lat = 0;

	/**
	 * Lon
	 */
	private double lon = 0;

	/**
	 * LocationAPI
	 */
	private LocationAPI locationAPI;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.hand_main);

		mContext = this.getApplicationContext();
		mApplication = this.getApplication();

		mMap = (MapView) findViewById(R.id.mapView);
		mMapController = mMap.getController();
		mMapController.setZoom(16);

		final Geocoder geo = new Geocoder(this, Locale.JAPAN);
		final MyLocationOverlay marker = new MyLocationOverlay(this, mMap);
		if (marker.enableMyLocation()) {
			marker.runOnFirstFix(new Runnable() {
				public void run() {
					GeoPoint point = marker.getMyLocation();
					mMap.getController().animateTo(point);
					mMap.getController().setZoom(16);
					try {
						double lat = Integer.valueOf(point.getLatitudeE6()).doubleValue() / 1e6;
						double lng = Integer.valueOf(point.getLongitudeE6()).doubleValue() / 1e6;
						List<Address> list = geo.getFromLocation(lat, lng, 1);
						if (list.isEmpty()) {
							return;
						}

					} catch (IOException e) {

					}
				}
			});
		} else {

		}

		// アイコンリソース取得
		mPin = getResources().getDrawable(R.drawable.pin);

		// SampleItemizedOverrayのインスタンスにアイコン登録
		mOverlay = new PinOverlay(mPin);
		mMap.getOverlays().add(mOverlay);

		GeoPoint point = new GeoPoint((int) (getLat() * 1e6), (int) (getLon() * 1e6));
		mMapController.animateTo(point);
		mOverlay.clearPoint();
		mOverlay.addPoint(point);

		locationAPI = new LocationAPI(mApplication);
		locationAPI.setEventListener(this);
		locationAPI.getGps();

		spinnerDevice = (Spinner) this.findViewById(R.id.device);
		ArrayAdapter<String> ADP = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

		// list of geiger counter
		ADP.add("SOEKS 01");
		ADP.add("RADEX RD1008");
		ADP.add("RADEX RD1706");
		ADP.add("RADEX RD1503+");
		ADP.add("RADEX RD1503");

		// add apinner
		spinnerDevice.setAdapter(ADP);

		// input textbox
		editText = (EditText) this.findViewById(R.id.editText);

		Button reloadButton = (Button) this.findViewById(R.id.buttonReload);

		reloadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				locationAPI.reloadGps();
			}
		});

		// upload button
		Button uploadButton = (Button) this.findViewById(R.id.buttonUp);
		final WebAPI webAPI = new WebAPI();
		webAPI.setEventListener(this);

		uploadButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String device = spinnerDevice.getSelectedItem().toString();
				String value = editText.getText().toString();

				if (lon == 0 && lat == 0) {
					Toast.makeText(mContext, "At first, you must get GPS", Toast.LENGTH_LONG).show();
				} else if (value != null && !value.equals("")) {
					String[] mKey = new String[] { "datetime", "label", "valuetype", "radiovalue", "lat", "lon" };
					String[] mValue = new String[] { getDataformat(), device, "0", value, "" + lon, "" + lat };
					webAPI.sendData(mKey, mValue);
					Toast.makeText(mContext, "Uploading data", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(mContext, "Input value", Toast.LENGTH_LONG).show();
				}

			}
		});

	}

	@Override
	public void onLoad(int type, String json) {
		Looper.prepare();
		Toast.makeText(mContext, "Finish upload", Toast.LENGTH_LONG).show();
		Looper.loop();
		editText.setText("");
	}

	/**
	 * Create date formati
	 */
	public String getDataformat() {
		final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
		String date = Settings.System.getString(this.getContentResolver(), DEFAULT_DATE_FORMAT);

		return date;
	}

	@Override
	public void onGpsLoad(double lat, double lon) {

		GeoPoint point = new GeoPoint((int) (lat * 1e6), (int) (lon * 1e6));
		mMapController.animateTo(point);

		mOverlay.clearPoint();
		mOverlay.addPoint(point);

		this.lat = lat;
		this.lon = lon;

		saveGps(lat, lon);

		locationAPI.removeGps();

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "AR Client");
		menu.add(Menu.NONE, 2, Menu.NONE, "ADK Client");
		menu.add(Menu.NONE, 3, Menu.NONE, "Bluetooth Client");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		Intent selectIntent = new Intent();
		switch (item.getItemId()) {

		case 1:
			selectIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.AR.ARClient");
			startActivity(selectIntent);
			break;
		case 2:
			selectIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.ADK.ADKClient");
			startActivity(selectIntent);
			break;
		case 3:
			selectIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.Bluetooth.BluetoothClient");
			startActivity(selectIntent);
			break;
		}
		return ret;
	}

	private void saveGps(double lat, double lon) {
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lat", "" + lat);
		editor.putString("lon", "" + lon);

		editor.commit();
	}

	private double getLat() {
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);
		String lat = settings.getString("lat", "37.487489");
		return Double.parseDouble(lat);
	}

	private double getLon() {
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);
		String lon = settings.getString("lon", "139.93017");
		return Double.parseDouble(lon);
	}

}
