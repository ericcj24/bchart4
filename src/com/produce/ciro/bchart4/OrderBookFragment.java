package com.produce.ciro.bchart4;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

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
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;


public class OrderBookFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final static String TAG = OrderBookFragment.class.getSimpleName();

	// Identifies a particular Loader being used in this component
	private static final int ORDERBOOK_LOADER = 1;
	
	private XYPlot _plot1;
	
	private XYSeries _series1;
	
	private XYSeries _series2;
	
	private static final NumberFormat _formatter = new DecimalFormat("#0.00");
	

	public OrderBookFragment() {
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
		getLoaderManager().initLoader(ORDERBOOK_LOADER, null, this);

		return inflater.inflate(R.layout.fragment_orderbook_chart, container, false);
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
		_plot1.setVisibility(TRIM_MEMORY_BACKGROUND);
		Log.i(TAG, "on pause");
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
			case ORDERBOOK_LOADER:
				// String buildUnionQuery (String[] subQueries, String sortOrder, String limit)

				// Returns a new CursorLoader
				String[] projection = { OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN,
				        OrderBookProviderContract.ORDERBOOK_KIND_COLUMN, OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN,
				        OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN };

				String selection = null;
				String sortOrder = OrderBookProviderContract.ORDERBOOK_TIMESTAMP_COLUMN + " DESC " + "LIMIT " + 300;
				return new CursorLoader(getActivity(), // Parent activity context
				        OrderBookProviderContract.ORDERBOOKURL_TABLE_CONTENTURI, // Table to query
				        projection, // Projection to return
				        selection, // No selection clause
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
		if (returnCursor != null) {
			plotOrderBook(returnCursor);
		}
		// mAdapter.changeCursor(returnCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Sets the Adapter's backing data to null. This prevents memory leaks.
		// mAdapter.changeCursor(null);
	}

	private void plotOrderBook(Cursor cursor) {
		_plot1 = (XYPlot) getView().findViewById(R.id.orderbookchart);
		_plot1.clear();

		List<Double> x1 = new ArrayList<Double>();
		List<Double> y1 = new ArrayList<Double>();
		List<Double> x2 = new ArrayList<Double>();
		List<Double> y2 = new ArrayList<Double>();

		Log.i(TAG, "cursor returned size is: " + String.valueOf(cursor.getCount()));
		int countAsk = 0;
		int countBid = 0;
		cursor.moveToFirst();
		while (cursor.isAfterLast() == false) {
			String type = cursor.getString(cursor.getColumnIndex(OrderBookProviderContract.ORDERBOOK_KIND_COLUMN));
			if (type.equals("ASK")) {
				// Log.i(TAG, "ask");
				String priceTemp = cursor.getString(cursor.getColumnIndex(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN));
				String amountTemp = cursor.getString(cursor.getColumnIndex(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN));
				double price = Double.parseDouble(priceTemp);
				double amount = Double.parseDouble(_formatter.format(Double.parseDouble(amountTemp)));
				//if (amount < 5) {
					x1.add(price);
					y1.add(amount);
					countAsk++;
				//}
			}
			if (type.equals("BID")) {
				// Log.i(TAG, "bid");
				String priceTemp = cursor.getString(cursor.getColumnIndex(OrderBookProviderContract.ORDERBOOK_PRICE_COLUMN));
				String amountTemp = cursor.getString(cursor.getColumnIndex(OrderBookProviderContract.ORDERBOOK_AMOUNT_COLUMN));
				double price = Double.parseDouble(priceTemp);
				double amount = Double.parseDouble(_formatter.format(Double.parseDouble(amountTemp)));
				//if (amount < 5) {
					x2.add(price);
					y2.add(amount);
					countBid++;
				//}
			}
			cursor.moveToNext();
		}
		Log.i(TAG, "ob plot ask size is: " + countAsk);
		Log.i(TAG, "ob plot bid size is: " + countBid);

		_series1 = new SimpleXYSeries(x1, y1, "Asks");
		_series2 = new SimpleXYSeries(x2, y2, "Bids");

		// Create a formatter to use for drawing a series using LineAndPointRenderer
		// and configure it from xml:
		LineAndPointFormatter series1Format = new LineAndPointFormatter();
		series1Format.setPointLabelFormatter(new PointLabelFormatter());
		series1Format.configure(getActivity().getApplicationContext(), R.xml.line_point_formatter_with_plf1);
		// set line to transparent
		series1Format.getLinePaint().setAlpha(0);

		// add a new series' to the xyplot:
		_plot1.addSeries(_series1, series1Format);

		// same as above:
		LineAndPointFormatter series2Format = new LineAndPointFormatter();
		series2Format.setPointLabelFormatter(new PointLabelFormatter());
		series2Format.configure(getActivity().getApplicationContext(), R.xml.line_point_formatter_with_plf2);
		// set line to transparent
		series2Format.getLinePaint().setAlpha(0);
		_plot1.addSeries(_series2, series2Format);

		// reduce the number of range labels
		_plot1.setTicksPerRangeLabel(3);
		_plot1.getGraphWidget().setDomainLabelOrientation(-45);

		// customize our domain/range labels
		_plot1.setDomainLabel("Price");
		_plot1.setRangeLabel("Amount");

		_plot1.redraw();
		_plot1.setVisibility(1);
		_plot1.bringToFront();
	}
}
