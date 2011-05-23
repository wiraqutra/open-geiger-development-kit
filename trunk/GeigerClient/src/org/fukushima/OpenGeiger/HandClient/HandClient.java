package org.fukushima.OpenGeiger.HandClient;

//import java.util.Set;
//import java.util.UUID;
//import org.fukushima.OpenGeiger.R;
import org.fukushima.OpenGeiger.R;

import android.app.Activity;
//import android.app.AlertDialog;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothSocket;
//import android.content.Context;
//import android.content.*;
import android.location.*;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;
import android.text.*;

public class HandClient extends Activity  {
	
	/**
	 * Tag
	 */
//	private static final String TAG = "HAND_CLIENT";
	

	public void onCreate(Bundle savedInstanceState) {
		/*
       	Location location = null; 
        final double IDo = location.getLatitude(); //IDo 緯度の値 double型
        final double KDo = location.getLongitude();//KDo 経度の値 double型        
       final String ido = String.valueOf(IDo);
       final String kdo = String.valueOf(KDo);
 
    	
    	super.onCreate(savedInstanceState);
        
    	requestWindowFeature(Window.FEATURE_NO_TITLE);
    	
    	setContentView(R.layout.main);
    	
    	Spinner KiS   = (Spinner)  this.findViewById(R.layout.main);
                KiS   = (Spinner)  this.findViewById(R.id.kisyu);
        //ArrayAdapter<String> ADP = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        //ガイガーカウンター機種  
        //ADP.add("放射能ハカール");  
        //ADP.add("測定器β");  
        //ADP.add("はかるくん２００");  
        //スピナーにADPを設定  
        Spinner spinner = (Spinner)this.findViewById(R.id.kisyu);  
        //spinner.setAdapter(ADP);  
        final String Kisyu = KiS.getSelectedItem().toString();
        
        //ImageView IM;
        
       	EditText editT;
        editT = (EditText) this.findViewById(R.layout.main);
        editT = (EditText) this.findViewById(R.id.ati);
        editT.setHeight(50);
        
        SpannableStringBuilder SEN = (SpannableStringBuilder) editT.getText();
        final String ATI = SEN.toString();
        Double.parseDouble(ATI);             //ATI 線量の値 double型
        
    	Button   BTN;
        BTN   = (Button) this.findViewById(R.layout.main);
        BTN   = (Button) this.findViewById(R.id.sosin);
        BTN.setOnClickListener(new OnClickListener(){
        	public void onClick(View view){
        		//送信ボタンを押したときの処理
        	        	
        		Log.i("機種",Kisyu);
        		Log.i("線量", ATI);
        		Log.i("緯度", ido);
        		Log.i("経度", kdo);
        		
        	}
        });
        
        */
    } 
    
    
    
}
	
