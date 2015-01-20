package com.produce.ciro.bchart4;

import android.app.Service;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;

public class BWTUpdateService extends Service{
	private static final String TAG = BWTUpdateService.class.getSimpleName();

	private TransactionUpdateHelper th = new TransactionUpdateHelper(this);
	private OrderBookUpdateHelper oh = new OrderBookUpdateHelper(this);
	
	@Override
	public void onCreate (){
		//listeners
		Log.i(TAG, "service create");
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		Log.i(TAG, "onStartCommand");
		String dataString = intent.getDataString();
		int flag = Integer.parseInt(dataString);
		
		ConnectivityManager cm =
		        (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
		 
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null &&
		                      activeNetwork.isConnectedOrConnecting();
		
		if(isConnected){
		
		if(flag == 1){
			Log.i(TAG, "too old, update entire database");
			th.firstCall();
			oh.firstCall();
		}
		
		
		Log.i(TAG, "starting orderbook and transaction");
		th.secondCall();
		oh.secondCall();
		}
		
		return START_NOT_STICKY;
		//START_STICKY
		//START_REDELIVER_INTENT
	}
	
	@Override
	public void onDestroy (){
		// Disconnect from the service (or become disconnected my network conditions)
		th.disconnect();
		oh.disconnect();
		
		Log.i(TAG, "service stop");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
}