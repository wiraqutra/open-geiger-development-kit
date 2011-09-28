package org.fukushima.OpenGeiger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.util.Log;

public class WebAPI {
	/**
	 * DEBUG
	 */
	private static final boolean DEBUG = true;

	/**
	 * TAG
	 */
	private static final String TAG = "WEB_API";

	/**
	 * BindするContext
	 */
	private Context mContext;

	/**
	 * Upload URL
	 */
	private static final String URL_UPLOAD = "http://geigerapi.appspot.com/upload";

	/**
	 * Action ID of Upload
	 */
	private static final int ACT_UPLOAD = 1;

	/**
	 * Event Listener
	 */
	private WebAPIListener mWebAPIListener;

	/**
	 * Constructor
	 */
	public WebAPI() {

	}

	/**
	 * Constructor
	 */
	public WebAPI(Context context) {
		this.mContext = context;
	}

	/**
	 * Upload data
	 * 
	 * @param key
	 * @param value
	 */
	public void sendData(String[] key, String[] value) {
		PostThread mPostThread = new PostThread(ACT_UPLOAD, URL_UPLOAD, key, value);
		mPostThread.start();
	}

	/**
	 * Thread for upload
	 * 
	 * @author gclue_akira
	 * 
	 */
	private class PostThread extends Thread {
		private String url;
		private int type;
		private List<NameValuePair> postParams;

		public PostThread(int type, String url, String[] key, String[] value) {
			this.url = url;
			this.type = type;
			postParams = new ArrayList<NameValuePair>();
			for (int i = 0; i < key.length; i++) {
				postParams.add(new BasicNameValuePair(key[i], value[i]));
			}
		}

		public void run() {
			HttpClient mHttp = new DefaultHttpClient();

			try {
				HttpPost postMethod = new HttpPost(url);

				// Header of Post
				postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded");

				// UrlEncode
				UrlEncodedFormEntity sendData = new UrlEncodedFormEntity(postParams, "UTF-8");
				postMethod.setEntity(sendData);

				// Connect
				HttpResponse mResponse = mHttp.execute(postMethod);
				if (DEBUG) {
					Log.i(TAG, "connecting");
				}

				// Response Code
				int resCode = mResponse.getStatusLine().getStatusCode();
				// Response Type
				String resType = mResponse.getEntity().getContentType().getValue();
				// Response Value
				HttpEntity httpEntity = mResponse.getEntity();
				String resValue = EntityUtils.toString(httpEntity);

				if (DEBUG) {
					Log.i(TAG, "resCode:" + resCode);
					Log.i(TAG, "resType:" + resType);
					Log.i(TAG, "resValue:" + resValue);
				}

				// OK
				if (resCode == HttpStatus.SC_OK) {
					mWebAPIListener.onLoad(type, resValue);
				}
				// NG
				else {

					mWebAPIListener.onLoad(type, "-1");
				}
			} catch (IOException e) {
				// Error
				mWebAPIListener.onLoad(type, "-1");
			}
		}
	}

	/**
	 * Eventリスナーを設定
	 * 
	 * @param listener
	 */
	public void setEventListener(WebAPIListener listener) {
		this.mWebAPIListener = listener;
	}
}