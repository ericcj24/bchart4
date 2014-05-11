package edu.illinois.jchen93.bitstampwebsockettest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
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


public class TransactionUpdateHelper{
	private static final String TAG = TransactionUpdateHelper.class.getSimpleName();
	
	private Context context;
	private static final String TPATH = "https://www.bitstamp.net/api/transactions/";
	private Pusher pusher;
	
	public TransactionUpdateHelper() {}
	
	public TransactionUpdateHelper(Context context) {
		this.context = context;
	}
	
	protected void firstCall(){
		new transactionFirstCall().execute();
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
		Channel channel = pusher.subscribe("live_trades");
		
		// Bind to listen for events called "my-event" sent to "my-channel"
		channel.bind("trade", new SubscriptionEventListener() {
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
	
	protected void disconnect(){
		pusher.disconnect();
		Log.i(TAG, "transaction close connection");
	}
	
	private class transactionFirstCall extends AsyncTask<Void, Void, ArrayList<Transaction>>{
		
		protected ArrayList<Transaction> doInBackground(Void... params) {
			ArrayList<Transaction> rt = new ArrayList<Transaction>();
			rt = fetchTransactions();
			return rt;
		}
	
		@Override
		protected void onPostExecute(ArrayList<Transaction> tlist) {			
			if(tlist!=null){
				addNewTransaction(tlist);
			}			
		}
	}
	
	private ArrayList<Transaction> fetchTransactions(){

		ArrayList<Transaction> rt = new ArrayList<Transaction>();
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
        		List<Transaction> transactionList = mapper.readValue(c.getInputStream(), new TypeReference<ArrayList<Transaction>>() { });       		
        		rt.addAll(transactionList);	
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
		return rt;
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
			Log.i(TAG, msg);
			
			ObjectMapper mapper = new ObjectMapper();          
			Trade t;
			try {
				t = mapper.readValue(msg, Trade.class);
				//Log.i(TAG, "price is: " + t.getPrice());
				//Log.i(TAG, "amount is: " + t.getAmount());
				//Log.i(TAG, "id is: " + t.getId());
				
				addNewSingleTransaction(t);
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
		
		private void addNewSingleTransaction(Trade t){
			ContentResolver cr = context.getContentResolver();
			// find all rows that has date bigger than newDate
			String selection = null;
			String[] projection = null;
			String[] selectionArgs = null;
			String sortOrder = null;
			Cursor cursor = cr.query(TransactionProviderContract.CONTENT_URI, 
									projection, 
									selection, 
									selectionArgs, 
									sortOrder);
			
			
			long unixTime = System.currentTimeMillis() / 1000L;
			//Log.i(TAG, "now date is: "+unixTime);
			
			ContentValues values = new ContentValues();
			values.put(TransactionProviderContract.TRANSACTION_TID_COLUMN, t.getId());
			values.put(TransactionProviderContract.TRANSACTION_DATE_COLUMN, unixTime);
			values.put(TransactionProviderContract.TRANSACTION_PRICE_COLUMN, t.getPrice());
			values.put(TransactionProviderContract.TRANSACTION_AMOUNT_COLUMN, t.getAmount());
			cr.insert(TransactionProviderContract.CONTENT_URI, values);					

			cursor.close();
		}
	}

	private int addNewTransaction(ArrayList<Transaction> lt){
		int count = 0;
		if(lt.size()>0){
		long newDate = Long.parseLong(lt.get(0).getDate());
		
		//Log.i(TAG, "database date is: "+newDate);
		ContentResolver cr = context.getContentResolver();
		// find all rows that has date bigger than newDate
		String selection = TransactionProviderContract.TRANSACTION_DATE_COLUMN + " = " + newDate;
		String[] projection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		Cursor cursor = cr.query(TransactionProviderContract.CONTENT_URI, 
								projection, 
								selection, 
								selectionArgs, 
								sortOrder);
					
		if (cursor.getCount()==0) {
			int size = lt.size();
			int i = 0;
			ContentValues[] values = new ContentValues[size];
			for (Transaction temp : lt){
				long entryTime = Long.parseLong(temp.getDate());
				values[i] = new ContentValues();
				values[i].put(TransactionProviderContract.TRANSACTION_TID_COLUMN, temp.getTid());
				values[i].put(TransactionProviderContract.TRANSACTION_DATE_COLUMN, entryTime);
				values[i].put(TransactionProviderContract.TRANSACTION_PRICE_COLUMN, temp.getPrice());
				values[i].put(TransactionProviderContract.TRANSACTION_AMOUNT_COLUMN, temp.getAmount());
				i++;
				count++;
			}
			cr.bulkInsert(TransactionProviderContract.CONTENT_URI, values);
		}
		cursor.close();}
		Log.i(TAG, "count size is: " + count);
		return count;
	}
	
}