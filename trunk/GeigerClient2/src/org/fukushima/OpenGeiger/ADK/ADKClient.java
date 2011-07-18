package org.fukushima.OpenGeiger.ADK;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
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
import org.fukushima.OpenGeiger.HandClient.PinOverlay;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import android.app.Application;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ADKClient extends MapActivity implements Runnable, OnClickListener, ClientThreadListener, ConnectedThreadListener, WebAPIListener, LocationAPIListener {
	
	/**
	 * Tag
	 */
	private static final String TAG = "ADK_CLIENT";
	
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
	 * Usb Manager
	 */
	private UsbManager mUsbManager;
	
	/**
	 * PendingIntent
	 */
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;

	/**
	 * Action Name of connecting USB
	 */
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
	
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
	
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
	
	int counter;
	
	 private MediaPlayer mMp; 
	 Vibrator vibrator;
	 
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	    
         
    	setContentView(R.layout.main);
        mContext = this.getBaseContext();
        mApplication = this.getApplication();
        vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        Resources res = this.getBaseContext().getResources();    
        
        // サウンドデータの読み込み(res/raw/sound.wav)    
        mMp = MediaPlayer.create(mContext, R.raw.sound);
        
        // USB接続
        mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
				ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);
        
		Log.i(TAG,"getLastNonConfigurationInstance()"+getLastNonConfigurationInstance());
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
		enableControls(false);
		
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
		enableControls(true);
		
        
    }
    
    @Override
	public void onResume() {
		super.onResume();

		Intent intent = getIntent();
		if (mInputStream != null && mOutputStream != null) {
			return;
		}

		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) {
				openAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
		//sendCommand((byte)2,(byte)0,(byte)255);
	}
    

	@Override
	public void onPause() {
		super.onPause();
		closeAccessory();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mUsbReceiver);
		super.onDestroy();
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
					int cpm = counter - msg.arg1;
					double sv = cpm * 0.00883*0.5;
					//double sv = cpm * 0.00200;
					NumberFormat format = NumberFormat.getInstance();
					format.setMaximumFractionDigits(4);
					mTextView.setText(""+format.format(sv));
					
					
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
	
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "BR:"+action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory "
								+ accessory);
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = UsbManager.getAccessory(intent);
				if (accessory != null && accessory.equals(mAccessory)) {
					closeAccessory();
				}
			}
		}
	};
	
	private void openAccessory(UsbAccessory accessory) {
		Log.d(TAG, "openAccessory");
		mFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mFileDescriptor != null) {
			mAccessory = accessory;
			FileDescriptor fd = mFileDescriptor.getFileDescriptor();
			mInputStream = new FileInputStream(fd);
			mOutputStream = new FileOutputStream(fd);
			Thread thread = new Thread(null, this, "DemoKit");
			thread.start();
			Log.d(TAG, "accessory opened");
			enableControls(true);
		} else {
			Log.d(TAG, "accessory open fail");
		}
	}

	private void closeAccessory() {
		enableControls(false);

		try {
			if (mFileDescriptor != null) {
				mFileDescriptor.close();
			}
		} catch (IOException e) {
		} finally {
			mFileDescriptor = null;
			mAccessory = null;
		}
	}
	
	protected void enableControls(boolean enable) {
	}
	
	
	
	private int composeInt(byte hi, byte lo) {
		int val = (int) hi & 0xff;
		val *= 256;
		val += (int) lo & 0xff;
		return val;
	}
	
	public void sendCommand(byte command, byte target, int value) {
		byte[] buffer = new byte[3];
		if (value > 255)
			value = 255;

		buffer[0] = command;
		buffer[1] = target;
		buffer[2] = (byte) value;
		if (mOutputStream != null && buffer[1] != -1) {
			try {
				mOutputStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "write failed", e);
			}
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode != KeyEvent.KEYCODE_BACK){
			return super.onKeyDown(keyCode, event);
		}else{
			this.finish();
			return false;
		}
	}
	
	@Override	
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;

		while (ret >= 0) {
			try {
				ret = mInputStream.read(buffer);
			} catch (IOException e) {
				break;
			}

			i = 0;
			while (i < ret) {
				int len = ret - i;

				switch (buffer[i]) {
				case 0x1:
					if (len >= 3) {
						
					}
					i += 3;
					break;

				case 0x4:
					if (len >= 3) {
						
					}
					i += 3;
					break;

				case 0x5:
					if (len >= 3) {
						
						Message m = Message.obtain(mHandler,CALC);
						int value = composeInt(buffer[i + 1],buffer[i + 2]);
						counter += value;
						m.arg1 = counter;
						mHandler.sendMessageDelayed(m, 60*1000);
						
						Log.i(TAG,"value:"+composeInt(buffer[i + 1],buffer[i + 2]));
						Log.i(TAG,"bf1:"+buffer[i + 1]);
						Log.i(TAG,"bf2:"+buffer[i + 2]);
						
						
					}
					i += 3;
					break;

				case 0x6:
					if (len >= 3) {
						
					}
					i += 3;
					break;

				default:
					Log.d(TAG, "unknown msg: " + buffer[i]);
					i = len;
					break;
				}
			}

		}
	}


}