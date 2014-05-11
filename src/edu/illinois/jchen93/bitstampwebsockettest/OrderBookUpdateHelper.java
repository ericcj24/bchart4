package edu.illinois.jchen93.bitstampwebsockettest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

// too many data, insertion database toooooo slow, consider trim the data upon receiving it

public class OrderBookUpdateHelper{
	private static final String TAG = OrderBookUpdateHelper.class.getSimpleName();
	
	private static final String TPATH = "https://www.bitstamp.net/api/order_book/";
	private Context context;
	private Pusher pusher;
	
	public OrderBookUpdateHelper() {}
	public OrderBookUpdateHelper(Context context){
		this.context = context;
		// Create a new Pusher instance
		this.pusher = new Pusher("de504dc5763aeef9ff52");
	}
	
	protected void firstCall(){
		new firstCall().execute();
	}
	
	protected void secondCall(){
		
		// pusher was initialized upon creation of this class
		pusher.connect(new ConnectionEventListener() {
		    @Override
		    public void onConnectionStateChange(ConnectionStateChange change) {
		        Log.i(TAG, "State changed to " + change.getCurrentState() +
		                           " from " + change.getPreviousState());
		    }

		    @Override
		    public void onError(String message, String code, Exception e) {
		        Log.i(TAG, "There was a problem connecting!");
		    }
		}, ConnectionState.ALL);

		// Subscribe to a channel
		Channel channel = pusher.subscribe("order_book");
		
		// Bind to listen for events called "my-event" sent to "my-channel"
		channel.bind("data", new SubscriptionEventListener() {
		    @Override
		    public void onEvent(String channel, String event, String data) {
		        //Log.i(TAG, "Received event with data: " + data);
		        log(data);
		    }
		    
		 // Logging helper method
			private void log(String msg) {
				LogTask task = new LogTask(msg);
				task.execute();
			}
		    
		});
	
		// with all channel subscriptions and event bindings automatically recreated
		pusher.connect();
		// The state change listener is notified when the connection has been re-established,
		// the subscription to "my-channel" and binding on "my-event" still exist.
	}
	
	private class LogTask extends AsyncTask<Void, Void, Void> {

		String msg;

		public LogTask(String msg) {
			this.msg = msg;
		}

		@Override
		protected Void doInBackground(Void... args) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {			
			ObjectMapper mapper = new ObjectMapper();          
			Orderbook1 ob;
			try {
				ob = mapper.readValue(msg, Orderbook1.class);
				Log.i(TAG, " websocket ask price is: " + ob.getAsks().get(0).get(0));
				Log.i(TAG, " websocket ask amoutn is: " + ob.getAsks().get(0).get(1));
				
				addNewSingleOrderbook(ob);
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
					
			super.onPostExecute(result);
		}
		
		private void addNewSingleOrderbook(Orderbook1 ob){
			ContentResolver cr = context.getContentResolver();
			// find all rows that has date bigger than newDate
			String selection = null;
			String[] projection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			Cursor cursor = cr.query(OrderBookProviderContract.CONTENT_URI, 
									projection, 
									selection, 
									selectionArgs, 
									sortOrder);
			
			int count = 0;
			long unixTime = System.currentTimeMillis() / 1000L;
			
			for(ArrayList<String> t : ob.getAsks()){
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, unixTime);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "ASK");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, t.get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, t.get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);
				count++;
			}
			for(ArrayList<String> t : ob.getBids()){
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, unixTime);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "BID");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, t.get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, t.get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);	
				count++;
			}
			Log.i(TAG,"stream count is: "+count);
			cursor.close();
		}
	}
	
	protected void disconnect(){
		boolean isConnected = (pusher.getConnection().getState() == ConnectionState.CONNECTED);
		if(isConnected) {
			pusher.disconnect();
			Log.i(TAG, "orderbook pusher close connection");
		}
	}
	
	private class firstCall extends AsyncTask<Void, Void, OrderBook>{
		
		protected OrderBook doInBackground(Void... params) {			
			OrderBook rt = fetchOrderbook();
			return rt;
		}
	
		@Override
		protected void onPostExecute(OrderBook ob) {			
			if(ob!=null){
				int count = addNewOrderbook(ob);
				Log.i(TAG, "count is: "+count);
			}			
		}
	}
	
	private OrderBook fetchOrderbook(){
		OrderBook ob = new OrderBook();        
        try {
        	URL url=new URL(TPATH);
            HttpURLConnection c=(HttpURLConnection)url.openConnection();
            c.setRequestMethod("GET");
        	c.setReadTimeout(15000);
        	c.connect();
            
        	int responseCode = c.getResponseCode();
        	//Log.i(TAG, "order book response code: " + Integer.toString(responseCode));
        	if (responseCode == 200){
        		ObjectMapper mapper = new ObjectMapper();
                ob = mapper.readValue(c.getInputStream(), OrderBook.class);
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
		return ob;
	}
	
	private int addNewOrderbook(OrderBook orderBook){
		int count = 0;		
		ContentResolver cr = context.getContentResolver();
		if(orderBook.getTimestamp()!=null){
		String timestamp = orderBook.getTimestamp();
		Long timeNow = Long.parseLong(timestamp);		
		//Log.i(TAG, " time now is: "+ timestamp);

		int askSize = orderBook.getAsks().size();
		int bidSize = orderBook.getBids().size();
		Log.i(TAG, " http ask size is: "+ askSize);
		Log.i(TAG, " http bid size is: "+ bidSize);
		
		// manually taking first 240+240 results
		ArrayList<ArrayList<String>> askList = orderBook.getAsks();
		int askInsertSize = 240;
		ArrayList<ArrayList<String>> bidList = orderBook.getBids();
		int bidInsertSize = 240;
		
		ContentValues[] values = new ContentValues[askInsertSize+bidInsertSize];
		int i=0;
		for(i=0; i<askInsertSize; i++){
			values[i] = new ContentValues();
			values[i].put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, timeNow);
			values[i].put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "ASK");
			values[i].put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, askList.get(i).get(0));
			values[i].put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, askList.get(i).get(1));
			count++;
		}
		
		for(int j=0; j<bidInsertSize; j++){
			values[i+j] = new ContentValues();
			values[i+j].put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, timeNow);
			values[i+j].put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "BID");
			values[i+j].put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, bidList.get(j).get(0));
			values[i+j].put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, bidList.get(j).get(1));
			count++;
		}
		cr.bulkInsert(OrderBookProviderContract.CONTENT_URI, values);}
		return count;
	}
}