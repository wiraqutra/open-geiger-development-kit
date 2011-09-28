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
import org.fukushima.OpenGeiger.Hand.PinOverlay;

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

public class ADKClient extends MapActivity implements Runnable, OnClickListener, WebAPIListener, LocationAPIListener {

	/**
	 * Tag
	 */
	private static final String TAG = "ADK_CLIENT";

	/**
	 * Context
	 */
	private Context mContext;

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

	/**
	 * PermissionRequestPending
	 */
	private boolean mPermissionRequestPending;

	/**
	 * Action Name of connecting USB
	 */
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";

	/**
	 * ImageView (adk)
	 */
	private ImageView icon_adk;

	/**
	 * ImageView (geiger)
	 */
	private ImageView icon_geiger;

	/**
	 * Upload button
	 */
	private Button buttonUpload;

	/**
	 * value of radioactivity
	 */
	private int now_value;

	/**
	 * CALCULATING
	 */
	final static int CALC = 1;

	/**
	 * ADK ICON
	 */
	final static int ICON_ADK = 2;

	final static int ICON_VISIBLE_GG = 3;
	final static int ICON_INVISIBLE_GG = 4;

	final static int CALCING = 5;
	final static int DEVICE = 10;

	private TextView mTextView;
	private TextView mTextViewDevice;
	private String value = "";
	Uri mImageUri;

	String mCPM = "";

	/**
	 * UsbAccessory
	 */
	private UsbAccessory mAccessory;

	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;

	/**
	 * Total Count
	 */
	private int counter;

	/**
	 * Media Player
	 */
	private MediaPlayer mMp;

	/**
	 * Vibrator
	 */
	private Vibrator vibrator;

	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.adk_main);
		mContext = this.getBaseContext();
		mApplication = this.getApplication();
		vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
		Resources res = this.getBaseContext().getResources();

		// load the data of sound
		mMp = MediaPlayer.create(mContext, R.raw.sound);

		// Upload Button
		buttonUpload = (Button) findViewById(R.id.Button03);
		buttonUpload.setOnClickListener(this);

		// get instance of Usb Manager
		mUsbManager = UsbManager.getInstance(this);
		mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		Log.i(TAG, "getLastNonConfigurationInstance()" + getLastNonConfigurationInstance());
		if (getLastNonConfigurationInstance() != null) {
			mAccessory = (UsbAccessory) getLastNonConfigurationInstance();
			openAccessory(mAccessory);
		}
		enableControls(true);

		mMap = (MapView) findViewById(R.id.mapView);
		mMapController = mMap.getController();
		mMapController.setZoom(16);

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

		// TextView of Value
		mTextView = (TextView) findViewById(R.id.value);

		// TextView of device
		mTextViewDevice = (TextView) findViewById(R.id.device);

		// icon
		icon_adk = (ImageView) findViewById(R.id.icon_adk);
		icon_geiger = (ImageView) findViewById(R.id.icon_geiger);

		// iconを非表示にする
		icon_adk.setVisibility(ImageView.VISIBLE);
		icon_geiger.setVisibility(ImageView.INVISIBLE);

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
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} else {
			Log.d(TAG, "mAccessory is null");
		}
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

		// Upload Button
		if (view.equals(buttonUpload)) {
			WebAPI webAPI = new WebAPI();
			webAPI.setEventListener(this);

			if (mCPM != null && !mCPM.equals("")) {
				String[] mKey = new String[] { "datetime", "label", "valuetype", "radiovalue", "lat", "lon" };
				String[] mValue = new String[] { getDataformat(), "Hack4Geiger", "0", mCPM, "" + lat, "" + lon };
				webAPI.sendData(mKey, mValue);
				Toast.makeText(this, "Upload data", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(this, "Can't upload", Toast.LENGTH_LONG).show();
			}

		}
	}

	final Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ICON_ADK:
				icon_adk.setVisibility(ImageView.VISIBLE);
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
				double sv = cpm * 0.00883 * 0.5;
				// double sv = cpm * 0.00200;
				NumberFormat format = NumberFormat.getInstance();
				format.setMaximumFractionDigits(4);
				mTextView.setText("" + format.format(sv));

				break;
			}
		}
	};

	@Override
	public void onLoad(int type, String json) {
		// TODO Auto-generated method stub
		Log.i(TAG, "SEND");
	}

	/**
	 * Create date formati
	 */
	public String getDataformat() {
		final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd hh:mm:ss";
		String date = Settings.System.getString(this.getContentResolver(), DEFAULT_DATE_FORMAT);

		return date;
	}

	/**
	 * Call when GPS loaded.
	 * 
	 * @param lat
	 * @param lon
	 */
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

	/**
	 * Menu
	 * 
	 * @param menu
	 * @return
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "AR Client");
		menu.add(Menu.NONE, 2, Menu.NONE, "Hand Client");
		menu.add(Menu.NONE, 3, Menu.NONE, "Bluetooth Client");
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
		case 1:
			Intent arIntent = new Intent();
			arIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.AR.ARClient");
			startActivity(arIntent);
			break;

		case 2:
			Intent handIntent = new Intent();
			handIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.Hand.HandClient");
			startActivity(handIntent);
			break;

		case 3:
			Intent bluetoothIntent = new Intent();
			bluetoothIntent.setClassName("org.fukushima.OpenGeiger", "org.fukushima.OpenGeiger.Bluetooth.BluetoothClient");
			startActivity(bluetoothIntent);
			break;
		default:
			break;
		}
		return ret;
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
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

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.d(TAG, "BR:" + action);
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = UsbManager.getAccessory(intent);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						openAccessory(accessory);
					} else {
						Log.d(TAG, "permission denied for accessory " + accessory);
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
		if (keyCode != KeyEvent.KEYCODE_BACK) {
			return super.onKeyDown(keyCode, event);
		} else {
			this.finish();
			this.moveTaskToBack(true);
			return false;
		}
	}

	@Override
	public void run() {
		int ret = 0;
		byte[] buffer = new byte[16384];
		int i;
		icon_geiger.setVisibility(ImageView.VISIBLE);
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

						Message m = Message.obtain(mHandler, CALC);
						int value = composeInt(buffer[i + 1], buffer[i + 2]);
						if (value > 0) {
							mMp.start();
							counter += value;
							m.arg1 = counter;
							mHandler.sendMessageDelayed(m, 60 * 1000);

							Log.i(TAG, "value:" + composeInt(buffer[i + 1], buffer[i + 2]));
							Log.i(TAG, "bf1:" + buffer[i + 1]);
							Log.i(TAG, "bf2:" + buffer[i + 2]);
						}

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