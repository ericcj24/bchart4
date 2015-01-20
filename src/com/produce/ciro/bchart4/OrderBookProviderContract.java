package com.produce.ciro.bchart4;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines constants for accessing the content provider defined in DataProvider. A content provider contract assists in accessing the
 * provider's available content URIs, column names, MIME types, and so forth, without having to know the actual values.
 */
public final class OrderBookProviderContract implements BaseColumns {

	private OrderBookProviderContract() {}

	// The URI scheme used for content URIs
	public static final String SCHEME = "content";

	// The provider's authority
	public static final String AUTHORITY = "com.produce.ciro.bchart4.orderbook";

	/**
	 * The MIME type for a content URI that would return multiple rows
	 * <P>
	 * Type: TEXT
	 * </P>
	 */
	public static final String MIME_TYPE_ROWS = "vnd.android.cursor.dir/vnd.com.produce.ciro.bchart4";

	/**
	 * The MIME type for a content URI that would return a single row
	 * <P>
	 * Type: TEXT
	 * </P>
	 * 
	 */
	public static final String MIME_TYPE_SINGLE_ROW = "vnd.android.cursor.item/vnd.com.produce.ciro.bchart4";

	/**
	 * The DataProvider content URI
	 */
	public static final Uri CONTENT_URI = Uri.parse(SCHEME + "://" + AUTHORITY);

	/**
	 * Order Book table name
	 */
	public static final String ORDERBOOK_TABLE_NAME = "OrderBookData";

	/**
	 * Order Book table content URI
	 */
	public static final Uri ORDERBOOKURL_TABLE_CONTENTURI = Uri.withAppendedPath(CONTENT_URI, ORDERBOOK_TABLE_NAME);

	/**
	 * Order Book table primary key column name
	 */
	public static final String ROW_ID = BaseColumns._ID;

	/**
	 * Order Book table timestamp column name
	 */
	public static final String ORDERBOOK_TIMESTAMP_COLUMN = "Timestamp";

	/**
	 * Order Book table kind column name
	 */
	public static final String ORDERBOOK_KIND_COLUMN = "Kind";

	/**
	 * Order Book table price column name
	 */
	public static final String ORDERBOOK_PRICE_COLUMN = "Price";

	/**
	 * Order Book table amount column name
	 */
	public static final String ORDERBOOK_AMOUNT_COLUMN = "Amount";

	// The content provider database name
	public static final String DATABASE_NAME = "OrderBookDataDB";

	// The starting version of the database
	public static final int DATABASE_VERSION = 1;

}
