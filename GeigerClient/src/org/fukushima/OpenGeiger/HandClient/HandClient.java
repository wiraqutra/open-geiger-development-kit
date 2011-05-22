package org.fukushima.OpenGeiger.HandClient;

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

public class HandClient extends Activity  {
	
	/**
	 * Tag
	 */
	private static final String TAG = "HAND_CLIENT";
	

	
    public void onCreate(Bundle savedInstanceState) {
    	
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	setContentView(R.layout.main);
      
        
    }
}
	
