package edu.illinois.jchen93.bitstampwebsockettest;

public class Trade{
	private String price;
	private String amount;
	private int id;
	public Trade(){}
	public String getPrice(){
		return price;
	}
	public String getAmount(){
		return amount;
	}
	public int getId(){
		return id;
	}
}