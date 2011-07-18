package org.fukushima.OpenGeiger.ARClient;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import org.fukushima.OpenGeiger.R;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

public class ARClient extends Activity implements Runnable,SensorEventListener,LocationListener{

	/**
	 * Tag
	 */
	private static final String TAG = "AR_CLIENT";
	
	/**
	 * Context
	 */
	private Context mContext;
	
	/**
	 * Application
	 */
	private Application mApplication;
	
	
	UsbAccessory mAccessory;
	ParcelFileDescriptor mFileDescriptor;
	FileInputStream mInputStream;
	FileOutputStream mOutputStream;
 private Preview mPreview;
 private MyView mMview;
 private boolean mRegisteredSensor;
 private SensorManager mSensorManager = null;  
 private LocationManager lm;
 /**
	 * Usb Manager
	 */
	private UsbManager mUsbManager;
	final static int CALC = 1;
	final static int ICON_BT = 2;
	final static int ICON_VISIBLE_GG = 3;
	final static int ICON_INVISIBLE_GG = 4;
	final static int CALCING = 5;
	final static int DEVICE = 10;
	/**
	 * PendingIntent
	 */
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	 private MediaPlayer mMp; 
	 Vibrator vibrator;
	 Drawable image;
	 
	/**
	 * Action Name of connecting USB
	 */
	private static final String ACTION_USB_PERMISSION = "com.google.android.DemoKit.action.USB_PERMISSION";
	
 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this.getBaseContext();
        mApplication = this.getApplication();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mRegisteredSensor = false;
        
        // LocationManagerでGPSの値を取得するための設定
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);    
        // 値が変化した際に呼び出されるリスナーの追加
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

        vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        Resources res = this.getBaseContext().getResources();    
        
        // サウンドデータの読み込み(res/raw/sound.wav)    
        mMp = MediaPlayer.create(mContext, R.raw.sound);
        // タイトルを消す
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    
        // カメラビュー用のSurfaceViewを生成しセットする
        mPreview = new Preview(this);
        setContentView(mPreview);
        
        mMview = new MyView(this);
        addContentView(mMview, new LayoutParams
                (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      // TODO Auto-generated method stub
      
      }

      public void onSensorChanged(SensorEvent event) {
      // TODO Auto-generated method stub
             if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
                mMview.setOrientationString((int)event.values[0],(int)event.values[1],(int)event.values[2]);
                
                // 方角を計算
                int direction = (int)event.values[0];
                // 方角を示す漢字
                String dirStringText = "取得失敗";
                
                // 0=North, 90=East, 180=South, 270=West
                if(direction >= -5 && direction <= 5 ){
                 dirStringText = "東";
                }
                else if(direction > 5 && direction < 85){
                 dirStringText = "南東";
                }
                else if(direction >= 85 && direction <= 95 ){
                 dirStringText = "南";
                }
                else if(direction > 95 && direction < 175){
                 dirStringText= "南西";
                }
                else if(direction >= 175 && direction <= 185 ){
                 dirStringText = "西";
                }
                else if(direction > 185 && direction < 265){
                 dirStringText = "北西";
                }
                else if(direction >= 265 && direction <= 275 ){
                 dirStringText = "北";
                }
                else if(direction > 275 && direction < 355){
                 dirStringText = "北東";
                }
                mMview.setDirection(dirStringText); 
                
             }

      
      }
         @Override
         protected void onResume() {
          List sensors = mSensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
          
          if (sensors.size() > 0) {
              Sensor sensor = (Sensor) sensors.get(0);
              mRegisteredSensor = mSensorManager.registerListener(this,
                  sensor,
                  SensorManager.SENSOR_DELAY_FASTEST);
          }  
          super.onResume();
         }
         
         @Override
         protected void onPause() {
             if (mRegisteredSensor) {
                 mSensorManager.unregisterListener(this);
                 mRegisteredSensor = false;
             }
             super.onPause();
         }
         
         @Override
         protected void onStop() {
             // unregister listener
          mSensorManager.unregisterListener(this);
             super.onStop();
         }
         
         // GPSの値が更新されると呼び出される
         public void onLocationChanged(Location location) {   
             mMview.setLocaionString(""+location.getLatitude(),""+location.getLongitude());
         }

         public void onProviderDisabled(String provider) {
             // TODO Auto-generated method stub
             
         }

         public void onProviderEnabled(String provider) {
             // TODO Auto-generated method stub
             
         }

         public void onStatusChanged(String provider, int status, Bundle extras) {
             // TODO Auto-generated method stub
             
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
     	
     	public void run() {
     		int ret = 0;
     		byte[] buffer = new byte[16384];
     		int i;
     		boolean flagCount = false;
     		
     		
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
     			
     				case 0x5:
     					if (len >= 3) {
     						Message m = Message.obtain(mHandler,CALC);
     						int value = composeInt(buffer[i + 1],buffer[i + 2]);
     						Log.d(TAG,"value="+value);
     						if(flagCount == false){
     							
     							/*
     							if(value < 100){
     								
     								m.arg1 = counter;
     								counter++;
     								flagCount= true;
     								mMp.start();  
     								
     								vibrator.vibrate(1*100);

     								mHandler.sendMessageDelayed(m, 60*1000);
     							}*/
     							
     						}
     						else{
     							if(value >= 100){
     								flagCount= false;
     							}
     						
     						}
     						
     						
     						
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
     	
    	final Handler mHandler = new Handler(){
    		public void handleMessage(Message msg){
    			switch (msg.what){
    				
    				case CALC:
    					
    					
    					break;
    			}
    		}
    	};

         
}
/**
 * オーバーレイ描画用のクラス
 */
class MyView extends View
{
    int x;
    int y;
    
    int ori_x;
    int ori_y;
    int ori_z;
    
    String dirString;
    
    String latString;
    String lonString;
    
    /**
     * コンストラクタ
     * @param c
     */
    public MyView(Context c)
    {
        super(c);
        setFocusable(true);
    }
    
   /**
    * 描画処理
    */
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        /* 背景色を設定 */
        canvas.drawColor(Color.TRANSPARENT);
        
        /* 描画するための線の色を設定 */
        Paint mainPaint = new Paint();
        mainPaint.setStyle(Paint.Style.FILL);
        mainPaint.setARGB(255, 255, 255, 100);
        
        /* 線で描画 */
        canvas.drawLine(x, y, 50, 50, mainPaint);
        
        /* 文字の色を設定 */
        Paint textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(25);
        textPaint.setARGB(255, 255, 0, 0);
        
        /* 文字を描画 */
        canvas.drawText("x"+ori_x, 20, 30, textPaint);
        canvas.drawText("y"+ori_y, 20, 50, textPaint);
        canvas.drawText("z"+ori_z, 20, 70, textPaint);
        canvas.drawText("lat"+latString, 20, 120, textPaint);
        canvas.drawText("lon"+lonString, 20, 140, textPaint);
        
        /* 文字の色を設定 */
        Paint dirTextPaint = new Paint();
        dirTextPaint.setStyle(Paint.Style.FILL);
        dirTextPaint.setARGB(255, 255, 0, 0);
        dirTextPaint.setTextSize(32);
        /* 文字を描画 */
        //canvas.drawText(dirString, 155, 100, dirTextPaint);
        
    }
    
    /**
     * タッチイベント
     */
    public boolean onTouchEvent(MotionEvent event) {
        
        /* X,Y座標の取得 */
        x = (int)event.getX();         
        y = (int)event.getY();
        /* 再描画の指示 */
        invalidate();
 
        return true;
    }
    
    /**
     * ロケーションの描画
     */
    public void setLocaionString(String lat, String lan) {
        latString = lat;
     lonString = lan;
       
     
        /* 再描画の指示 */
        invalidate();
    }
    
    /**
     * 方角の描画
     */
    public void setOrientationString(int new_x, int new_y, int new_z) {
        
        /* x,y,zの値の設定 */
     ori_x = new_x;
     ori_y = new_y;
     ori_z = new_z;
     
        /* 再描画の指示 */
        invalidate();
    }
    
    /**
     * 方角の描画
     */
    public void setDirection(String newDirString) {
        
     /* 方角 */
     dirString = newDirString;
     
        /* 再描画の指示 */
        invalidate();
    }
}

/**
 * カメラビュー
 * @author GClue
 *
 */
class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    
    Preview(Context context) {
        super(context);
        
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * SurfaceViewが生成されるタイミングに呼び出される
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, acquire the camera and tell it where
        // to draw.
        mCamera = Camera.open();
        try {
           mCamera.setPreviewDisplay(holder);
        } catch (IOException exception) {
            mCamera.release();
            mCamera = null;
            // TODO: add more exception handling logic here
        }
    }

    /**
     * SurfaceViewが破棄されるタイミングに呼び出される
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
        mCamera.stopPreview();
        mCamera = null;
    }

    /**
     *  SurfaceViewのサイズが修正されたときに呼び出される
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(w, h);
        //mCamera.setParameters(parameters);
        mCamera.startPreview();
    }
    
}