package org.fukushima.OpenGeiger.Bluetooth;

import java.util.Set;
import java.util.UUID;

import org.fukushima.OpenGeiger.ClientThread;
import org.fukushima.OpenGeiger.ClientThreadListener;
import org.fukushima.OpenGeiger.ConnectedThread;
import org.fukushima.OpenGeiger.ConnectedThreadListener;
import org.fukushima.OpenGeiger.LocationAPI;
import org.fukushima.OpenGeiger.LocationAPIListener;
import org.fukushima.OpenGeiger.R;
import org.fukushima.OpenGeiger.WebAPI;
import org.fukushima.OpenGeiger.WebAPIListener;
import org.fukushima.OpenGeiger.Hand.PinOverlay;
import org.fukushima.OpenGeiger.R.id;
import org.fukushima.OpenGeiger.R.layout;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GeigerClient extends MapActivity implements OnClickListener, ClientThreadListener, ConnectedThreadListener, WebAPIListener, LocationAPIListener {
	
	/**
	 * Tag
	 */
	private static final String TAG = "BLUETOOTH_SAMPLE";
	
	/**
	 * Bluetooth Adapter
	 */
	private BluetoothAdapter mAdapter;
	
	/**
	 * Bluetooth Devices
	 */
	private BluetoothDevice mDevice;
	
	/**
	 * Bluetooth UUID
	 */
	private final UUID MY_UUID = UUID.fromString( "00001101-0000-1000-8000-00805F9B34FB" );
	
	/**
	 * Device Name
	 */
	private final String DEVICE_NAME = "FireFly-9CB3";
	
	/**
	 * Context
	 */
	private Context mContext;
	
	/** 
	 * Bluetooth Client Thread
	 */
	private ClientThread clientThread;
	
	/**
	 * Connected Thread
	 */
	private ConnectedThread connection;
	
	/**
	 * LocationAPI
	 */
	private LocationAPI locationAPI;
	
	/**
	 * Lon
	 */
	private double lon;
	
	/**
	 * Lat
	 */
	private double lat;
	
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
	 * Application
	 */
	private Application mApplication;
	
	/**
	 * ImageView
	 */
	ImageView icon_bluetooth;
	ImageView icon_geiger;
	
	
	Button buttonOn;
	Button buttonUpload;
	
	int now_value;
	
	final String NAME = "BLUETOOTH_SAMPLE";
	final static int CALC = 1;
	final static int ICON_BT = 2;
	final static int ICON_VISIBLE_GG = 3;
	final static int ICON_INVISIBLE_GG = 4;
	final static int CALCING = 5;
	final static int DEVICE = 10;
	TextView mTextView;
	TextView mTextViewDevice;
	String value = "";
	Uri mImageUri;
	
	String mCPM = "";
	
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	setContentView(R.layout.main);
        mContext = this.getBaseContext();
        mApplication = this.getApplication();
        
        mMap = (MapView)findViewById(R.id.mapView); 
    	mMapController = mMap.getController();
    	mMapController.setZoom(16);
    	
    	//アイコンリソース取得
        mPin = getResources().getDrawable( R.drawable.pin);
        
    	 //SampleItemizedOverrayのインスタンスにアイコン登録
        mOverlay = new PinOverlay(mPin);
        mMap.getOverlays().add(mOverlay);
        
        
        GeoPoint point = new GeoPoint((int)(getLat() * 1e6),  (int)(getLon() * 1e6));  
        mMapController.animateTo(point);  
        mOverlay.clearPoint();
        mOverlay.addPoint(point);
        
    	locationAPI = new LocationAPI(mApplication);
		locationAPI.setEventListener(this);
    	locationAPI.getGps();
    	
        // Satrt Server Button
        buttonOn = (Button)findViewById(R.id.Button02);
        buttonOn.setOnClickListener(this);
        
        // Satrt Server Button
        buttonUpload = (Button)findViewById(R.id.Button03);
        buttonUpload.setOnClickListener(this);
       
        // TextView of Value
        mTextView = (TextView)findViewById(R.id.value);
        
        // TextView of device
        mTextViewDevice = (TextView)findViewById(R.id.device);
        
        // icon
        icon_bluetooth = (ImageView)findViewById(R.id.icon_bluetooth);
        icon_geiger = (ImageView)findViewById(R.id.icon_geiger);
        
        icon_bluetooth.setVisibility(ImageView.INVISIBLE);
        icon_geiger.setVisibility(ImageView.INVISIBLE);
        
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        Set< BluetoothDevice > devices = mAdapter.getBondedDevices();
        
		for ( BluetoothDevice device : devices ){
			Log.i(TAG,"DEVICE:"+device.getName());
			if(device.getName().equals(DEVICE_NAME)){
				mDevice = device;
			}
		}
		
		
        
    }
    
	@Override
	public void onClick(View view) {
		// Start Button 
		if(view.equals(buttonOn)){
			try{
				clientThread = new ClientThread(mDevice, MY_UUID );
				clientThread.setListener(this);
				mAdapter.cancelDiscovery();
    		
				clientThread.start();
				locationAPI.getGps();
			}
			catch(Exception e){
				Toast.makeText(this, "Can't connect GeigerCouner", Toast.LENGTH_LONG).show();
			}
		}
		// Upload Button
		else if(view.equals(buttonUpload)){
			WebAPI webAPI = new WebAPI();
    		webAPI.setEventListener(this);
    		
    		if(mCPM != null && !mCPM.equals("")){
	    		String[] mKey = new String[] { "datetime","label","valuetype","radiovalue","lat","lon"};
	    		String[] mValue = new String[] { getDataformat(),"Hack4Geiger","0",mCPM,""+lat,""+lon};
	    		webAPI.sendData(mKey, mValue);
	    		Toast.makeText(this, "Upload data", Toast.LENGTH_LONG).show();
    		}
    		else{
    			Toast.makeText(this, "Can't upload", Toast.LENGTH_LONG).show();
    		}
         
		}
	}
	
	/**
	 * Connected Socket
	 * @param socket
	 */
	public void manageConnectedSocket( BluetoothSocket socket){
		Log.i(TAG,"Connection");
		
		Message msgBt = new Message();
		msgBt.what = ICON_BT;
		mHandler.sendMessage(msgBt);
		
		Message msgDevice = new Message();
		msgDevice.what = DEVICE;
		mHandler.sendMessage(msgDevice);
		
		Message msgCalcing = new Message();
		msgDevice.what = CALCING;
		mHandler.sendMessage(msgCalcing);
		
		icon_bluetooth.setEnabled(true);
		connection = new ConnectedThread(socket);
		connection.setListener(this);
		connection.start();
    }
	
	@Override
	public void onConnect(BluetoothSocket mSocket) {
		manageConnectedSocket(mSocket);
	}

	@Override
	public void onResult(int value) {
		Log.i(TAG,"Result:"+value);
		now_value = value;
		mCPM = ""+value;
		
		Message msg = new Message();
		msg.arg1 = value;
		msg.what = CALC;
		mHandler.sendMessage(msg);
		
	}
	
	final Handler mHandler = new Handler(){
		public void handleMessage(Message msg){
			switch (msg.what){
				case DEVICE:
					mTextViewDevice.setText(DEVICE_NAME);
					break;
				case ICON_BT:
					icon_bluetooth.setVisibility(ImageView.VISIBLE);
					break;
				case ICON_VISIBLE_GG:
					icon_geiger.setVisibility(ImageView.VISIBLE);
					break;
				case ICON_INVISIBLE_GG:
					icon_geiger.setVisibility(ImageView.INVISIBLE);
					break;
				case CALCING:
					mTextView.setText("Calc");
					break;
				case CALC:
					mTextView.setText(mCPM);
					Message mMsg = new Message();
					mMsg.what = ICON_VISIBLE_GG;
					mHandler.sendMessage(mMsg);
					
					break;
			}
		}
	};

	@Override
	public void onLoad(int type, String json) {
		// TODO Auto-generated method stub
		Log.i(TAG,"SEND");
		
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
        
        saveGps(lat,lon);
        
        locationAPI.removeGps();
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 1, Menu.NONE, "Hand Client");
        return super.onCreateOptionsMenu(menu);
    }
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean ret = true;
        switch (item.getItemId()) {
        case 1:
        	Intent selectIntent = new Intent();
        	selectIntent.setClassName("org.fukushima.OpenGeiger","org.fukushima.OpenGeiger.HandClient.HandClient");
        	startActivity(selectIntent);
            break;
        }
        return ret;
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
	
	private void saveGps(double lat, double lon){
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);

		SharedPreferences.Editor editor = settings.edit();
		editor.putString("lat", ""+lat);
		editor.putString("lon", ""+lon);

		editor.commit();
	}
	
	private double getLat(){
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);
		String lat = settings.getString("lat","37.487489");
		return Double.parseDouble(lat);
	}
	
	private double getLon(){
		// MYDATAという名前のSharedPreference
		SharedPreferences settings = mContext.getSharedPreferences("MYDATA", mContext.MODE_PRIVATE);
		String lon = settings.getString("lon","139.93017");
		return Double.parseDouble(lon);
	}	
}