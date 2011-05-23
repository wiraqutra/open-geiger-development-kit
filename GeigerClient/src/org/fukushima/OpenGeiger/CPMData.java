package org.fukushima.OpenGeiger;

import java.util.Vector;

import android.util.Log;

/**
 * CPM Data
 * @author akira
 */
public class CPMData{
	/**
	 * Tag
	 */
	private static final String TAG = "CPM_DATA";
	
	/**
	 * Count
	 */
	private static final int COUNT = 120;
	
	//Vector のインスタンス化
	Vector mVectorCPM;
	
	public CPMData(){
		mVectorCPM = new Vector();
	}
	
	public int getCount(){
		try{
			return mVectorCPM.size();
		}
		catch(Exception e){
			return 0;
		}
		
	}
	
	public void insertValue(int value){
		Log.i(TAG,"count="+getCount());
		if(getCount()<120){
			mVectorCPM.add(value);
		}
		else{
			mVectorCPM.remove(0);
			mVectorCPM.add(value);
		}
	}
	
	public int getCPM(){
		if(getCount()<COUNT){
			return -1;
		}
		else{
			int cpm = 0;
			for(int i = 0; i < COUNT; i++){
				cpm += (Integer)mVectorCPM.get(i);
				Log.i(TAG,i+"="+(Integer)mVectorCPM.get(i));
			}
			return cpm;
		}
	}

}