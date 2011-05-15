package org.fukushima.OpenGeiger;

import java.util.Set;
import java.util.UUID;
import org.fukushima.OpenGeiger.R;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class GeigerClient extends Activity implements OnClickListener, ClientThreadListener, ConnectedThreadListener {
	
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
	ClientThread clientThread;
	
	/**
	 * Connected Thread
	 */
	ConnectedThread connection;
	
	/**
	 * ImageView
	 */
	ImageView icon_bluetooth;
	ImageView icon_geiger;
	
	Button button01;
	Button button02;
	Button button03;
	
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
	
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	setContentView(R.layout.main);
        mContext = this.getBaseContext();
        
        // Satrt Server Button
        button02 = (Button)findViewById(R.id.Button02);
        button02.setOnClickListener(this);
       
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
		if(view.equals(button02)){
    		clientThread = new ClientThread(mDevice, MY_UUID );
    		clientThread.setListener(this);
    		mAdapter.cancelDiscovery();
    		
    		clientThread.start();
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
		Message msg = new Message();
		msg.arg1 = value;
		msg.what = CALC;
		mHandler.sendMessageDelayed(msg, 60*1000);
		
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
					int old_value = msg.arg1;
					if(now_value >= old_value){
						Message mMsg = new Message();
						mMsg.what = ICON_VISIBLE_GG;
						mHandler.sendMessage(mMsg);
						float value = (now_value - old_value)*85/1000;
						mTextView.setText(""+value);
					}
					else{
						Message mMsg = new Message();
						mMsg.what = ICON_INVISIBLE_GG;
						mHandler.sendMessage(mMsg);
					}
					break;
			}
		}
	};
	
}