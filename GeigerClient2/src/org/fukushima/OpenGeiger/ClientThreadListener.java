package org.fukushima.OpenGeiger;

import android.bluetooth.BluetoothSocket;

/**
 * ClientThreadListerner
 * @author akira
 */
public interface ClientThreadListener {
	/**
	 * After connect bluetooth's socket, return the socket.
	 * @param mSocket
	 */
	public void onConnect(BluetoothSocket mSocket);
}
