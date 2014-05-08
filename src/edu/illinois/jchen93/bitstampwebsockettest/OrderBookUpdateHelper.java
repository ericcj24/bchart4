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
	}
	
	protected void firstCall(){
		new firstCall().execute();
	}
	
	protected void secondCall(){
		// Create a new Pusher instance
		pusher = new Pusher("de504dc5763aeef9ff52");

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
				Log.i(TAG, "ask price is: " + ob.getAsks().get(0).get(0));
				Log.i(TAG, "ask amoutn is: " + ob.getAsks().get(0).get(1));
				
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
			
			long unixTime = System.currentTimeMillis() / 1000L;
			
			for(ArrayList<String> t : ob.getAsks()){
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, unixTime);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "ASK");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, t.get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, t.get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);					
			}
			for(ArrayList<String> t : ob.getBids()){
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, unixTime);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "BID");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, t.get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, t.get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);					
			}
			cursor.close();
		}
	}
	
	protected void disconnect(){
		pusher.disconnect();
		Log.i(TAG, "orderbook close connection");
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
	
		String timestamp = orderBook.getTimestamp();
		Long timeNow = Long.parseLong(timestamp);		
		Log.i(TAG, " time now is: "+ timestamp);

		int askSize = orderBook.getAsks().size();
		int bidSize = orderBook.getBids().size();
		Log.i(TAG, " ask size is: "+ askSize);
		Log.i(TAG, " bid size is: "+ bidSize);
		ArrayList<ArrayList<String>> askList = orderBook.getAsks();
		for(int i=0; i<askList.size()/10; i++){		
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, timeNow);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "ASK");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, askList.get(i).get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, askList.get(i).get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);
				count++;			
		}

		ArrayList<ArrayList<String>> bidList = orderBook.getBids();
		for(int i=0; i<bidList.size()/10; i++){			
				ContentValues values = new ContentValues();
				values.put(OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN, timeNow);
				values.put(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, "BID");
				values.put(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN, bidList.get(i).get(0));
				values.put(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN, bidList.get(i).get(1));
				cr.insert(OrderBookProviderContract.CONTENT_URI, values);
				count++;
		}				
		return count;
	}
	
	
	
}