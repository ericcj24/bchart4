package edu.illinois.jchen93.bitstampwebsockettest;


import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pusher.client.Pusher;
import com.pusher.client.channel.Channel;
import com.pusher.client.channel.SubscriptionEventListener;
import com.pusher.client.connection.ConnectionEventListener;
import com.pusher.client.connection.ConnectionState;
import com.pusher.client.connection.ConnectionStateChange;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	TextView tView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		tView = (TextView) findViewById(R.id.data);
		
		doSth();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
		
	private void doSth() {
		
		// Create a new Pusher instance
		Pusher pusher = new Pusher("de504dc5763aeef9ff52");

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
		        Log.i(TAG, "Received event with data: " + data);
                log(data);
		    }
		    
		 // Logging helper method
			private void log(String msg) {
				LogTask task = new LogTask(tView, msg);
				task.execute();
			}
		    
		});

		// Disconnect from the service (or become disconnected my network conditions)
		pusher.disconnect();

		// Reconnect, with all channel subscriptions and event bindings automatically recreated
		pusher.connect();
		// The state change listener is notified when the connection has been re-established,
		// the subscription to "my-channel" and binding on "my-event" still exist.
		
	}
		

	class LogTask extends AsyncTask<Void, Void, Void> {

		TextView view;
		String msg;

		public LogTask(TextView view, String msg) {
			this.view = view;
			this.msg = msg;
		}

		@Override
		protected Void doInBackground(Void... args) {
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			System.out.println(msg);

			String currentLog = view.getText().toString();
			String newLog = msg + "\n" + currentLog;
			
			 /*ObjectMapper mapper = new ObjectMapper();
            try {
				Trade t = mapper.readValue(data, Trade.class);
				Log.i(TAG, "price is: " + t.getPrice());
				Log.i(TAG, "amount is: " + t.getAmount());
				Log.i(TAG, "id is: " + t.getId());
				
			} catch (JsonParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			view.setText(newLog);

			super.onPostExecute(result);
		}

	}
}
