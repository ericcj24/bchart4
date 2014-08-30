package com.produce.ciro.bchart4;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.PointLabelFormatter;
import com.androidplot.xy.PointLabeler;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = TransactionFragment.class.getSimpleName();

	// Identifies a particular Loader being used in this component
	private static final int TRANSACTION_LOADER = 0;

	public TransactionFragment() {
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

		/*
		 * Initializes the CursorLoader. The URL_LOADER value is eventually passed to onCreateLoader().
		 */
		getLoaderManager().initLoader(TRANSACTION_LOADER, null, this);

		return inflater.inflate(R.layout.fragment_chart, container, false);
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
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "on pause");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.i(TAG, "on detach");
	}

	private void plotTransaction(Cursor cursor) {
		XYPlot plot1 = (XYPlot) getView().findViewById(R.id.chart);
		plot1.clear();

		int n = cursor.getCount();
		Log.i(TAG, "ploting transaction size is: " + n);
		Number[] time = new Number[n];
		Number[] y = new Number[n];
		int i = 0;
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			time[i] = Long.parseLong(cursor.getString(cursor.getColumnIndex(TransactionProviderContract.TRANSACTION_DATE_COLUMN)));
			y[i] = Double.parseDouble(cursor.getString(cursor.getColumnIndex(TransactionProviderContract.TRANSACTION_PRICE_COLUMN)));
			i++;
			cursor.moveToNext();
		}

		XYSeries series = new SimpleXYSeries(Arrays.asList(time), Arrays.asList(y), "Transactions");

		LineAndPointFormatter seriesFormat = new LineAndPointFormatter();
		seriesFormat.setPointLabelFormatter(new PointLabelFormatter());
		seriesFormat.configure(getActivity().getApplicationContext(), R.xml.line_point_formatter);
		seriesFormat.setPointLabeler(new PointLabeler() {
			@Override
			public String getLabel(XYSeries series, int index) {
				return index % 10 == 0 ? series.getY(index) + "" : "";
			}
		});
		plot1.addSeries(series, seriesFormat);

		// reduce the number of range labels
		plot1.setTicksPerRangeLabel(3);
		plot1.getGraphWidget().setDomainLabelOrientation(-45);

		// customize our domain/range labels
		plot1.setDomainLabel("Time");
		plot1.setRangeLabel("Price");

		plot1.setDomainValueFormat(new Format() {

			// create a simple date format that draws on the year portion of our timestamp.
			// see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
			// for a full description of SimpleDateFormat.
			private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

				// because our timestamps are in seconds and SimpleDateFormat expects milliseconds
				// we multiply our timestamp by 1000:
				long timestamp = ((Number) obj).longValue() * 1000;
				Date date = new Date(timestamp);
				return dateFormat.format(date, toAppendTo, pos);
			}

			@Override
			public Object parseObject(String source, ParsePosition pos) {
				return null;

			}
		});

		plot1.redraw();
		plot1.setVisibility(1);
		plot1.bringToFront();
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
			case TRANSACTION_LOADER:
				// Returns a new CursorLoader
				String[] projection = { TransactionProviderContract.TRANSACTION_DATE_COLUMN,
				        TransactionProviderContract.TRANSACTION_TID_COLUMN, TransactionProviderContract.TRANSACTION_PRICE_COLUMN,
				        TransactionProviderContract.TRANSACTION_AMOUNT_COLUMN };
				String sortOrder = TransactionProviderContract.TRANSACTION_TID_COLUMN + " DESC" + " LIMIT " + 2000;
				return new CursorLoader(getActivity(), // Parent activity context
				        TransactionProviderContract.TRANSACTIONURL_TABLE_CONTENTURI, // Table to query
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
			plotTransaction(returnCursor);

		// mAdapter.changeCursor(returnCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Sets the Adapter's backing data to null. This prevents memory leaks.
		// mAdapter.changeCursor(null);
	}

}
