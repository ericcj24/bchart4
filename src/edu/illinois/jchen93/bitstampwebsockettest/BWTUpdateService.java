package edu.illinois.jchen93.bitstampwebsockettest;


import android.app.Service;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;



public class BWTUpdateService extends Service{
	static final String TAG = BWTUpdateService.class.getSimpleName();

	static final String TPATH = "https://www.bitstamp.net/api/transactions/";

	private TransactionUpdateHelper th = new TransactionUpdateHelper(this);
	private OrderBookUpdateHelper oh = new OrderBookUpdateHelper(this);
	
	@Override
	public void onCreate (){
		//listeners
	}	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		
		th.firstCall();
		th.secondCall();
		Log.i(TAG, "starting orderbook");
		oh.firstCall();
		oh.secondCall();	
		
		return START_NOT_STICKY;
		//START_STICKY
		//START_REDELIVER_INTENT
	}
	
	@Override
	public void onDestroy (){
		// Disconnect from the service (or become disconnected my network conditions)
		th.disconnect();
		oh.disconnect();
		Log.i(TAG, "pusher disconnect");
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	

}