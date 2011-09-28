package org.fukushima.OpenGeiger;

import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationAPI implements LocationListener {

	/**
	 * TAG
	 */
	private static final String TAG = "LOCATION_API";

	/**
	 * LocationManager
	 */
	private LocationManager lm;

	/**
	 * Event Listener
	 */
	private LocationAPIListener mLocationAPIListener;

	/**
	 * Application
	 */
	private Application mApplication;

	public LocationAPI(Application application) {
		mApplication = application;

	}

	public void getGps() {
		// LocationManagerでGPSの値を取得するための設定
		lm = (LocationManager) mApplication.getSystemService(Context.LOCATION_SERVICE);
		// 値が変化した際に呼び出されるリスナーの追加
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 1, this);
	}

	public void reloadGps() {
		lm.removeUpdates(this);
		// LocationManagerでGPSの値を取得するための設定
		lm = (LocationManager) mApplication.getSystemService(Context.LOCATION_SERVICE);
		// 値が変化した際に呼び出されるリスナーの追加
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30 * 1000, 1, this);
	}

	public void removeGps() {
		lm.removeUpdates(this);
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		Log.i("GPS", "lat=" + location.getLatitude());
		Log.i("GPS", "lon=" + location.getLongitude());
		mLocationAPIListener.onGpsLoad(location.getLatitude(), location.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

	/**
	 * Eventリスナーを設定
	 * 
	 * @param listener
	 */
	public void setEventListener(LocationAPIListener listener) {
		this.mLocationAPIListener = listener;
	}

}