package edu.illinois.jchen93.bitstampwebsockettest;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import android.app.LoaderManager;
import android.app.Activity;
import android.app.Fragment;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TransactionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
	
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
		Log.i(TAG, "on createView");
		
		/*
         * Initializes the CursorLoader. The URL_LOADER value is eventually passed
         * to onCreateLoader().
         */
		getLoaderManager().initLoader(TRANSACTION_LOADER, null, this);
		
        return inflater.inflate(R.layout.fragment_chart, container, false);
    }
	
	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		Log.i(TAG, "on attach");
	}
	
	@Override
	public void onResume(){
		super.onResume();
		Log.i(TAG, "on resume");   	
	}
	
	@Override
	public void onPause(){
		super.onPause();
		Log.i(TAG, "on pause");
	}
	
	@Override
	public void onDetach() {
    	super.onDetach();
    	Log.i(TAG, "on detach");
    }
	
	
	private void plotTransaction(Cursor cursor){
		XYPlot plot1 = (XYPlot) getView().findViewById(R.id.chart);
		plot1.clear();
		
		int n = cursor.getCount();
		//Log.i(TAG, "ploting transaction size is: "+n);
		Number[] time = new Number[n];
		Number[] y = new Number[n];
		int i = 0;
		cursor.moveToFirst();	
		while (cursor.isAfterLast() == false){
			time[i] = Long.parseLong(cursor.getString(cursor.getColumnIndex(TransactionProviderContract.TRANSACTION_DATE_COLUMN)));
			y[i] = Double.parseDouble(cursor.getString(cursor.getColumnIndex(TransactionProviderContract.TRANSACTION_PRICE_COLUMN)));
			i++;
			cursor.moveToNext();
		}
		

		XYSeries series = new SimpleXYSeries(Arrays.asList(time),Arrays.asList(y),"Transactions");
		
		plot1.getGraphWidget().getGridBackgroundPaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getDomainGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getDomainGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getRangeGridLinePaint().setColor(Color.WHITE);
        plot1.getGraphWidget().getRangeGridLinePaint().
                setPathEffect(new DashPathEffect(new float[]{1, 1}, 1));
        plot1.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        plot1.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);

        // Create a formatter to use for drawing a series using LineAndPointRenderer:
        LineAndPointFormatter format = new LineAndPointFormatter(
                Color.RED,                  // line color
                Color.rgb(0, 100, 0),       // point color
                null, null);                // fill color
        
        plot1.getGraphWidget().setPaddingRight(2);
        plot1.addSeries(series, format);

        // draw a domain tick for each time:
        //plot1.setDomainStep(XYStepMode.SUBDIVIDE, time.length/400);
        plot1.setDomainStepValue(10);
        plot1.setRangeStepValue(9);

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
       
        plot1.getGraphWidget().getDomainLabelPaint().setTextSize(15);
        plot1.getGraphWidget().getRangeLabelPaint().setTextSize(15);
        //plot1.getGraphWidget().setMarginTop(18);
        //plot1.getGraphWidget().setMarginRight(10);
        //plot1.getGraphWidget().setDomainLabelOrientation(-20);
        //plot1.getGraphWidget().setRangeLabelOrientation(20);
        //plot1.getGraphWidget().setPaddingRight(25);
        //plot1.getGraphWidget().setPaddingTop(8);
        
        plot1.getGraphWidget().setSize(new SizeMetrics(70, SizeLayoutType.FILL, 50, SizeLayoutType.FILL));

        plot1.redraw();
        plot1.setVisibility(1);
        plot1.bringToFront();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader, so we don't care about the ID.
        // First, pick the base URI to use depending on whether we are
        // currently filtering.
		/*
	     * Takes action based on the ID of the Loader that's being created
	     */
	    switch (id) {
	        case TRANSACTION_LOADER:
	            // Returns a new CursorLoader
	        	String[] projection = {TransactionProviderContract.TRANSACTION_DATE_COLUMN,
	        						TransactionProviderContract.TRANSACTION_TID_COLUMN,
	        						TransactionProviderContract.TRANSACTION_PRICE_COLUMN,
	        						TransactionProviderContract.TRANSACTION_AMOUNT_COLUMN};
	        	String sortOrder = TransactionProviderContract.TRANSACTION_TID_COLUMN + " DESC" +" LIMIT " + 2000;
	            return new CursorLoader(
	                        getActivity(),   // Parent activity context
	                        TransactionProviderContract.TRANSACTIONURL_TABLE_CONTENTURI, // Table to query
	                        projection,      // Projection to return
	                        null,            // No selection clause
	                        null,            // No selection arguments
	                        sortOrder        // Default sort order
	            			);
	        default:
	            // An invalid id was passed in
	            return null;
	   }
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor returnCursor) {
		
		/*
         * Moves the query results into the adapter, causing the
         * ListView fronting this adapter to re-display
         */
		if(returnCursor!=null)
        plotTransaction(returnCursor);
        
        //mAdapter.changeCursor(returnCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// Sets the Adapter's backing data to null. This prevents memory leaks.
        //mAdapter.changeCursor(null);
	}
	
}