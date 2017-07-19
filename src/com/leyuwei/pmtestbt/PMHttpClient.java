package com.leyuwei.pmtestbt;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class PMHttpClient {
	public static final String URL_LOGIN = "pm_login.php";
	public static final String URL_REGISTER = "pm_register.php";
	public static final String URL_BACKUP = "pm_backup.php";
	public static final String URL_RECOVER = "pm_recover.php";
	public static final String URL_BIGDATA = "pm_bigdata.php";
	public static final String URL_MACADDR = "pm_macaddr.php";
	private static final String BASE_URL = "http://111.111.111.111/pmtest/";

	private static AsyncHttpClient client = new AsyncHttpClient();

	public static void get(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.get(context,getAbsoluteUrl(url), params, responseHandler);
	}

	public static void post(Context context, String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		client.post(context, getAbsoluteUrl(url), params, responseHandler);
	}
	
	public static void stopAllConnection(Context context){
		client.cancelRequests(context, true);
	}

	private static String getAbsoluteUrl(String relativeUrl) {
		return BASE_URL + relativeUrl;
	}
}
