package org.fukushima.OpenGeiger;

/**
 * Connected Thread Listener
 * @author akira
 */
public interface ConnectedThreadListener {
	
	/**
	 * Return value of count
	 * @param value value of count
	 */
	public void onResult(int value);
}
