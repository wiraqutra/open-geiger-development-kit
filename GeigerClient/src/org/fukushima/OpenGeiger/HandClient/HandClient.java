package org.fukushima.OpenGeiger.HandClient;

import org.fukushima.OpenGeiger.LocationAPI;
import org.fukushima.OpenGeiger.LocationAPIListener;
import org.fukushima.OpenGeiger.R;
import org.fukushima.OpenGeiger.WebAPI;
import org.fukushima.OpenGeiger.WebAPIListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
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
    	
    	mMap = (MapView)findViewById(R.id.mapView); 
    	mMapController = mMap.getController();
    	mMapController.setZoom(16);
    	
    	//アイコンリソース取得
        mPin = getResources().getDrawable( R.drawable.pin);
  
        //SampleItemizedOverrayのインスタンスにアイコン登録
        mOverlay = new PinOverlay(mPin);
        mMap.getOverlays().add(mOverlay);
        
    	locationAPI = new LocationAPI(mApplication);
		locationAPI.setEventListener(this);
    	locationAPI.getGps();
    	 
    	 
    	
    	
    	spinnerDevice   = (Spinner)  this.findViewById(R.id.device);
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
       	
       	/*
        // input Lat
       	editLat = (EditText) this.findViewById(R.id.editLat);
       	
        // input Lon
       	editLon = (EditText) this.findViewById(R.id.editLon);
       	
       	// Get GPS
       	Button gpsButton   = (Button) this.findViewById(R.id.buttonGPS);
       	final LocationAPI locationAPI = new LocationAPI(mApplication);
		locationAPI.setEventListener(this);
		
       	gpsButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View view){
        		locationAPI.getGps();
        	}
       	});
       	*/

       	Button reloadButton   = (Button) this.findViewById(R.id.buttonReload);
       	
		
		reloadButton.setOnClickListener(new OnClickListener(){
			public void onClick(View view){
				locationAPI.reloadGps();
			}
		});
		
       	// upload button
       	Button uploadButton   = (Button) this.findViewById(R.id.buttonUp);
       	final WebAPI webAPI = new WebAPI();
		webAPI.setEventListener(this);
		
		uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View view){
    		 	String device = spinnerDevice.getSelectedItem().toString();
    		 	String value = editText.getText().toString();
        		
        		if(lon == 0 && lat == 0){
        			Toast.makeText(mContext, "At first, you must get GPS", Toast.LENGTH_LONG).show();
        		}
        		else if(value != null && !value.equals("")){ 	
        			String[] mKey = new String[] { "datetime","label","valuetype","radiovalue","lat","lon"};
    	    		String[] mValue = new String[] { getDataformat(),device,"0",value,""+lon,""+lat};
    	    		webAPI.sendData(mKey, mValue);
    	    		Toast.makeText(mContext, "Uploading data", Toast.LENGTH_LONG).show();
        		}
        		else{
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
	public String getDataformat(){
		final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
        String date = Settings.System.getString(this.getContentResolver(), DEFAULT_DATE_FORMAT);
        
		return date;	
	}

	@Override
	public void onGpsLoad(double lat, double lon) {	
		GeoPoint point = new GeoPoint((int)(lat * 1e6),  (int)(lon * 1e6));  
        mMapController.animateTo(point);  
        
        mOverlay.clearPoint();
        mOverlay.addPoint(point);
        
        
        this.lat = lat;
        this.lon = lon;
        
        locationAPI.removeGps();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	
	
}
	
