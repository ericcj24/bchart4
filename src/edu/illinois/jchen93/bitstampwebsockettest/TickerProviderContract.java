package edu.illinois.jchen93.bitstampwebsockettest;

import android.net.Uri;
import android.provider.BaseColumns;


public final class TickerProviderContract implements BaseColumns {

    private TickerProviderContract() { }
        
    // The URI scheme used for content URIs
    public static final String SCHEME = "content";

    // The provider's authority
    public static final String AUTHORITY = "edu.illinois.jchen93.bitstampwebsockettest.ticker";

	/**
     *  The MIME type for a content URI that would return multiple rows
     *  <P>Type: TEXT</P>
     */
    public static final String MIME_TYPE_ROWS =
            "vnd.android.cursor.dir/vnd.edu.illinois.jchen93.bitstampwebsockettest";

    /**
     * The MIME type for a content URI that would return a single row
     *  <P>Type: TEXT</P>
     *
     */
    public static final String MIME_TYPE_SINGLE_ROW =
            "vnd.android.cursor.item/vnd.edu.illinois.jchen93.bitstampwebsockettest";
    
    
    /**
     * The DataProvider content URI
     */
    public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);
    
    /**
     * TICKER table name
     */
    public static final String TICKER_TABLE_NAME = "TickerData";

    /**
     * TICKER table content URI
     */
    public static final Uri TICKERURL_TABLE_CONTENTURI =
            Uri.withAppendedPath(CONTENT_URI, TICKER_TABLE_NAME);


    /**
     * TICKER table primary key column name
     */
    public static final String ROW_ID = BaseColumns._ID;
    
    /**
     * TICKER table timestamp column name
     */
    public static final String TICKER_TIMESTAMP_COLUMN = "Timestamp";
    
    /**
     * TICKER table high column name
     */
    public static final String TICKER_HIGH_COLUMN = "High";
    
    /**
     * TICKER table low column name
     */
    public static final String TICKER_LOW_COLUMN = "Low";
    
    /**
     * TICKER table last column name
     */
    public static final String TICKER_LAST_COLUMN = "Last";
    
    /**
     * TICKER table bid column name
     */
    public static final String TICKER_BID_COLUMN = "Bid";
    
    /**
     * TICKER table ask column name
     */
    public static final String TICKER_ASK_COLUMN = "Ask";
    
    /**
     * TICKER table vwap column name
     */
    public static final String TICKER_VWAP_COLUMN = "Vwap";
    
    /**
     * TICKER table volume column name
     */
    public static final String TICKER_VOLUME_COLUMN = "Volume";
    
    
    // The content provider database name
    public static final String DATABASE_NAME = "TickerDataDB";

    // The starting version of the database
    public static final int DATABASE_VERSION = 1;
}
