package org.fukushima.OpenGeiger;

import java.io.IOException;
import java.util.UUID;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * Bluetooth Client Thread
 * @author akira
 */
public class ClientThread extends Thread {
	
	/**
	 * Tag
	 */
	private static final String TAG = "BLUETOOTH_CLIENT_THREAD";
	
	/**
	 * BluetoothSocket
	 */
    private final BluetoothSocket mmSocket;
    
    /**
     * BluetoohDevice
     */
    private final BluetoothDevice mmDevice;
    
    /**
     * ClientThread
     * @param device Bluetooth device
     * @param MY_UUID UUID
     */
    public ClientThread(BluetoothDevice device, UUID MY_UUID) {
    	
        BluetoothSocket tmp = null;
        mmDevice = device;

        try {
            // create sockect
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
        	Log.i(TAG,"Error:"+e);
        }
        mmSocket = tmp;
    }

    public void run() {
       
        try {
        	// Connect 
            mmSocket.connect();
        } catch (IOException connectException) {
            // Unable connect, close socket.
            try {
                mmSocket.close();
            } catch (IOException closeException) { }
            return;
        }

        clientThreadListener.onConnect(mmSocket);
    }

    /**
     * Cancel sockect
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }
    
    
    /**
     *  Listener
     */
	private ClientThreadListener clientThreadListener;

	/**
	 * Set listerner.
	 * @param listener
	 */
	public void setListener(ClientThreadListener listener) {
		this.clientThreadListener = listener;
	}
    
}
