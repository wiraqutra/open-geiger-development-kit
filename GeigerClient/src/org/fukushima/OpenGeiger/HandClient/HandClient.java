package org.fukushima.OpenGeiger.HandClient;

import org.fukushima.OpenGeiger.LocationAPI;
import org.fukushima.OpenGeiger.LocationAPIListener;
import org.fukushima.OpenGeiger.R;
import org.fukushima.OpenGeiger.WebAPI;
import org.fukushima.OpenGeiger.WebAPIListener;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

public class HandClient extends Activity implements WebAPIListener, LocationAPIListener {
	
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
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setContentView(R.layout.hand_main);
    	
    	mContext = this.getApplicationContext();
    	mApplication = this.getApplication();
    	
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
       	
       	
       	// upload button
       	Button uploadButton   = (Button) this.findViewById(R.id.buttonUp);
       	final WebAPI webAPI = new WebAPI();
		webAPI.setEventListener(this);
		
		uploadButton.setOnClickListener(new OnClickListener(){
        	public void onClick(View view){
    		 	String device = spinnerDevice.getSelectedItem().toString();
    		 	String value = editText.getText().toString();
        		String lon = editLon.getText().toString();
        		String lat = editLat.getText().toString();
        		if(lon == null || lat == null){
        			Toast.makeText(mContext, "At first, you must get GPS", Toast.LENGTH_LONG).show();
        		}
        		else if(value != null && !value.equals("")){ 	
        			String[] mKey = new String[] { "datetime","label","valuetype","radiovalue","lat","lon"};
    	    		String[] mValue = new String[] { getDataformat(),device,"0",value,lon,lat};
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
		editLat.setText(""+lat);
		editLon.setText(""+lon);		
	}

	
	
}
	
