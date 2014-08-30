package edu.illinois.jchen93.bchart4;
import java.util.ArrayList;
/*
 * Order Book class used to bind JSON data from Order Book API
 * https://www.bitstamp.net/api/order_book/
 * example JSON data:
	{"bids": [["572.33", "0.17000000"], ["572.32", "0.17100000"],...]}
*/
	
public class Orderbook1 {
    private ArrayList<ArrayList<String>> bids;
    private ArrayList<ArrayList<String>> asks;
    public Orderbook1() {}
    
    public ArrayList<ArrayList<String>> getBids(){
    	return bids;
    }
    public ArrayList<ArrayList<String>> getAsks(){
    	return asks;
    }
}