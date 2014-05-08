package edu.illinois.jchen93.bitstampwebsockettest;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.util.Log;


public class TickerUpdateService extends IntentService{
	
	private static final String TAG = TickerUpdateService.class.getSimpleName();
	
	static final String TPATH = "https://www.bitstamp.net/api/ticker/";
	static long databaseDate = 0;
	
	/**
	   * A constructor is required, and must call the super IntentService(String)
	   * constructor with a name for the worker thread.
	   */
	public TickerUpdateService() {
		super("TickerUpdateService");
	}
		
	@Override
  protected void onHandleIntent(Intent workIntent) {
		//Log.i(TAG, this.toString());
		// Gets data from the incoming Intent
		String dataString = workIntent.getDataString();
        
		fetchTicker();
	}
	
	
	private void fetchTicker(){
		
		try {
			URL url=new URL(TPATH);
			HttpURLConnection c=(HttpURLConnection)url.openConnection();
			c.setRequestMethod("GET");
			c.setReadTimeout(15000);
			c.connect();
  
			int responseCode = c.getResponseCode();
			//Log.i(TAG, "response code: " + Integer.toString(responseCode));
			if (responseCode == 200){
				ObjectMapper mapper = new ObjectMapper();
				Ticker ticker = mapper.readValue(c.getInputStream(), Ticker.class);
  		
				addNewTicker(ticker);
		}
      
		}catch(java.net.ConnectException e){
			Log.e(TAG, e.toString());        	
		}catch(java.net.UnknownHostException e){
			Log.e(TAG, e.toString());
		}catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(TAG, e.toString());
		}finally{
			//c.disconnect();
		} 
	}
	
	private void addNewTicker(Ticker ticker){
		
		long newDate = Long.parseLong(ticker.getTimestamp());
				
		if(newDate > databaseDate){
			ContentResolver cr = getContentResolver();
			// find all rows that has date bigger than newDate
			//String selection = null;
			//String[] projection = null;
			//String[] selectionArgs = null;
			//String sortOrder = null;
			//Cursor cursor = cr.query(TickerProviderContract.CONTENT_URI, 
			//						projection, 
			//						selection, 
			//						selectionArgs, 
			//						sortOrder);
						
			//if (cursor.getCount()==0) {
				long timeLong = newDate*1000;
				SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
            	String formattedDate =  sdf.format(timeLong);
            	
				ContentValues values = new ContentValues();
				values.put(TickerProviderContract.TICKER_TIMESTAMP_COLUMN, formattedDate);
				values.put(TickerProviderContract.TICKER_HIGH_COLUMN, ticker.getHigh());
				values.put(TickerProviderContract.TICKER_LOW_COLUMN, ticker.getLow());
				values.put(TickerProviderContract.TICKER_LAST_COLUMN, ticker.getLast());
				values.put(TickerProviderContract.TICKER_BID_COLUMN, ticker.getBid());
				values.put(TickerProviderContract.TICKER_ASK_COLUMN, ticker.getAsk());
				values.put(TickerProviderContract.TICKER_VWAP_COLUMN, ticker.getVwap());
				values.put(TickerProviderContract.TICKER_VOLUME_COLUMN, ticker.getVolume());
				cr.insert(TickerProviderContract.CONTENT_URI, values);
				
			//}
			//cursor.close();
			databaseDate = newDate;
		}
	}
	
	
}