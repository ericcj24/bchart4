package com.produce.ciro.bchart4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;

public class NetworkReceiver extends BroadcastReceiver {
	public static final String TAG = "NetworkReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

		if (isNetworkDown) {
			Log.i(TAG, "internet down");
			CharSequence text = "No internet connection";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();

		}
		else {
			Log.i(TAG, "internet up");
			CharSequence text = "Internet connected";
			int duration = Toast.LENGTH_LONG;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}
}
