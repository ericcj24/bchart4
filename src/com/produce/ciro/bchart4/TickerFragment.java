package com.produce.ciro.bchart4;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class TickerFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = TickerFragment.class.getSimpleName();

	// Identifies a particular Loader being used in this component
	private static final int TICKER_LOADER = 2;
	private SimpleCursorAdapter mAdapter;
	private AlarmManager alarmMgr;
	private int REQUEST_CODE = 103;

	public TickerFragment() {
		// Empty constructor required for fragment subclasses
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "on create");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		Log.i(TAG, "on createView");

		View localView = inflater.inflate(R.layout.fragment_ticker, container, false);

		String[] mFromColumns = { TickerProviderContract.TICKER_TIMESTAMP_COLUMN, TickerProviderContract.TICKER_HIGH_COLUMN,
		        TickerProviderContract.TICKER_LOW_COLUMN, TickerProviderContract.TICKER_LAST_COLUMN,
		        TickerProviderContract.TICKER_BID_COLUMN, TickerProviderContract.TICKER_ASK_COLUMN,
		        TickerProviderContract.TICKER_VWAP_COLUMN, TickerProviderContract.TICKER_VOLUME_COLUMN };
		int[] mToFields = { R.id.timestamp, R.id.high, R.id.low, R.id.last, R.id.bid, R.id.ask, R.id.vwap, R.id.volume };

		// Gets a handle to a List View
		ListView mListView = (ListView) localView.findViewById(R.id.tickerList);
		// Defines a SimpleCursorAdapter for the ListView
		mAdapter = new SimpleCursorAdapter(getActivity(), // Current context
		        R.layout.ticker_items, // Layout for a single row
		        null, // No Cursor yet
		        mFromColumns, // Cursor columns to use
		        mToFields, // Layout fields to use
		        0 // No flags
		);
		// Sets the adapter for the view
		mListView.setAdapter(mAdapter);

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(TICKER_LOADER, null, this);

		return localView;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.i(TAG, "on attach");
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "on resume");

		String CHOICE = "2";
		Intent intent = new Intent(getActivity(), TickerUpdateService.class);
		intent.setData(Uri.parse(CHOICE));
		PendingIntent pendingIntent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, 0);
		alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME, 0, 2500, pendingIntent);
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "on pause");

		String CHOICE = "2";
		Intent intent = new Intent(getActivity(), TickerUpdateService.class);
		intent.setData(Uri.parse(CHOICE));

		boolean alarmUp = (PendingIntent.getService(getActivity(), REQUEST_CODE, intent, PendingIntent.FLAG_NO_CREATE) != null);
		if (alarmUp) {
			Log.i(TAG, "ticker alarm is cancelling");
			// pendingIntent.cancel();
			PendingIntent pendingIntent = PendingIntent.getService(getActivity(), REQUEST_CODE, intent, 0);
			alarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
			alarmMgr.cancel(pendingIntent);
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "on detach");
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader, so we don't care about the ID.
		// First, pick the base URI to use depending on whether we are
		// currently filtering.
		/*
		 * Takes action based on the ID of the Loader that's being created
		 */
		switch (id) {
			case TICKER_LOADER:
				// Returns a new CursorLoader
				String[] projection = { TickerProviderContract.ROW_ID, TickerProviderContract.TICKER_TIMESTAMP_COLUMN,
				        TickerProviderContract.TICKER_HIGH_COLUMN, TickerProviderContract.TICKER_LOW_COLUMN,
				        TickerProviderContract.TICKER_LAST_COLUMN, TickerProviderContract.TICKER_BID_COLUMN,
				        TickerProviderContract.TICKER_ASK_COLUMN, TickerProviderContract.TICKER_VWAP_COLUMN,
				        TickerProviderContract.TICKER_VOLUME_COLUMN };
				String sortOrder = TickerProviderContract.TICKER_TIMESTAMP_COLUMN + " DESC" + " LIMIT " + 1;
				return new CursorLoader(getActivity(), // Parent activity context
				        TickerProviderContract.TICKERURL_TABLE_CONTENTURI, // Table to query
				        projection, // Projection to return
				        null, // No selection clause
				        null, // No selection arguments
				        sortOrder // Default sort order
				);
			default:
				// An invalid id was passed in
				return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor returnCursor) {

		/*
		 * Moves the query results into the adapter, causing the ListView fronting this adapter to re-display
		 */
		if (returnCursor != null)
			mAdapter.changeCursor(returnCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Sets the Adapter's backing data to null. This prevents memory leaks.
		mAdapter.changeCursor(null);
	}

}
