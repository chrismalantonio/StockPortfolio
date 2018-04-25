package edu.temple.stockportfolio;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Chris on 4/20/2018.
 */

public class Stock {

        private String name, symbol, chart;
        private double price;


    public Stock(String name, String symbol, double price, String chart){
        this.name = name;
        this.symbol = symbol;
        this.price = price;
        this.chart = chart;
    }

    public Stock (JSONObject stockObject) throws JSONException{
        this(stockObject.getString("Name"), stockObject.getString("Symbol"), stockObject.getDouble("LastPrice"), stockObject.getString("Chart"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getChart(){
        return chart;
    }

    public void setChart(String chart){
        this.chart = chart;
    }

    @Override
    public boolean equals(Object object){
        Stock otherStock = (Stock) object;
        return this.symbol.equalsIgnoreCase(otherStock.getSymbol());
    }

    public JSONObject getStockAsJSON(){
        JSONObject stockObject = new JSONObject();
        try {
            stockObject.put("name", name);
            stockObject.put("symbol", symbol);
            stockObject.put("price", price);
            stockObject.put("chart", chart);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return stockObject;
    }

}
