package edu.illinois.jchen93.bitstampwebsockettest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import android.widget.Toast;


public class NetworkReceiver extends BroadcastReceiver{
	public static final String TAG = "NetworkReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent){
		
		boolean isNetworkDown = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		
		if(isNetworkDown){
			CharSequence text = "onReceive: NOT connected, stopping UpdateService";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
			
		}else{
			CharSequence text = "onReceive: connected, starting UpdateService";
			int duration = Toast.LENGTH_SHORT;
			Toast toast = Toast.makeText(context, text, duration);
			toast.show();

		}
	}
}