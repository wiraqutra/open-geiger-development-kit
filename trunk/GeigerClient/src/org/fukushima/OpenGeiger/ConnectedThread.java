package org.fukushima.OpenGeiger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Connected Thread
 * @author akira
 */
public class ConnectedThread extends Thread {
	
	/**
	 * Tag
	 */
	private static final String TAG = "BLUETOOTH_CONNECTED_THREAD";
	
	/**
	 * Bluetooth Socket
	 */
    private final BluetoothSocket mmSocket;
    
    /**
     * Input Stream
     */
    private final InputStream mmInStream;
    
    /**
     * OutputStream
     */
    private final OutputStream mmOutStream;
    
    /**
     * Calc
     */
    private final int CALC = 1;
    
    /**
     * Now Count
     */
    private int now_count;
    
    /**
     * Start command
     */
  private static final byte[] cmdStart = {'s','t','t',0xd};
        
    /**
     * Connected Thread
     * @param socket socket of connection
     */
    public ConnectedThread(BluetoothSocket socket) {
    	Log.i(TAG,"ConnectedThread");
        
    	mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        
        // Get inputstream and output stream
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        
    }

    /**
     * Start thread
     */
    public void run() {
    	Log.i(TAG,"ConnectionThread#run()");
    	
    	try {
			mmOutStream.write( "stt¥n".getBytes() );
		} catch (IOException e1) {
			Log.i(TAG,"Error:"+e1);
		}
		CPMData mCPMData = new CPMData();
    	
        byte[] buffer = new byte[1024];  // buffer store for the stream
        int bytes; // bytes returned from read()
       
        // Keep listening to the InputStream until an exception occurs
        while (true) {
            try {
            	
            
            	 
            	
                // Read from the InputStream
                bytes = mmInStream.read(buffer);
                
                String readMsg = new String(buffer, 0, bytes);
                Log.i(TAG,"readMsg:"+readMsg);
                if(readMsg.trim() != null && !readMsg.trim().equals("")){
                	int value = Integer.parseInt(readMsg.trim());
                	
                	mCPMData.insertValue(value);
                	Log.i(TAG,"CPM="+mCPMData.getCPM());
                	connectedThreadListener.onResult(mCPMData.getCPM());
                }
                
                // regrex ( 30:1 )
                /*
                Pattern pattern = Pattern.compile("([0-9]*):([0-9]*)");
                Matcher matcher = pattern.matcher(readMsg);
                boolean blnMatch= matcher.find();
                
                // if match format, get the count.
                if(blnMatch){
                	Log.d( TAG , "COUNT: " + matcher.group(1) );
                	String value = matcher.group(1);
                	if(value != null && !value.equals("")){
                		connectedThreadListener.onResult(Integer.parseInt(matcher.group(1)));
                	}
                }
                */
                
               
                // Sleep 10 sec
                
                /*
                try {
					Thread.sleep(10*1000);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				}
				*/
                
            } catch (IOException e) {
                break;
            }
        }
    }

 
    /**
     * Close socket
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
    
    /**
     * Listener
     */
	private ConnectedThreadListener connectedThreadListener;

	/**
	 * Set listerner.
	 * 
	 * @param listener
	 */
	public void setListener(ConnectedThreadListener listener) {
		this.connectedThreadListener = listener;
	}
	
	
	
}