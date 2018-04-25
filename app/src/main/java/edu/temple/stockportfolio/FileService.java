package edu.temple.stockportfolio;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FileService extends IntentService {

    private static final String STOCK_INFO = "StockList";
    private static final String COMPANY_NAME = "Name";
    private static final String COMPANY_SYMBOL = "Symbol";
    private static final String COMPANY_STOCK_PRICE = "LastPrice";
    private static final String COMPANY_STOCK_CHART = "Chart";
    public static final String UPDATE_FILE = "Updating file";

    private Stock[] stockList;

    public FileService() {
        super("FileService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            }
            //read stocks from file
            while (true){
                //update file with new stock info every 60 seconds
                SystemClock.sleep(30000);
                File file = new File(getFilesDir(), "StockPortfolioFile");
                if (file != null) {
                    JSONObject stockJSON = readStocksFromFile(file);
                    if (stockJSON.length() != 0) {
                        parseStockJSON(stockJSON);
                    }
                try {
                    file = new File(getFilesDir(), "StockPortfolioFile");
                    FileOutputStream outputStream = new FileOutputStream(file);
                    StringBuffer fileContents = new StringBuffer("{\"StockList\":[");
                    for (int i = 0; i < stockList.length; i++) {
                        String stockAPI = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + stockList[i].getSymbol();
                        String response;
                        try {
                            URL url = new URL(stockAPI);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            InputStream input = new BufferedInputStream(conn.getInputStream());
                            response = getJSONAsString(input);
                            JSONObject stockInfo = new JSONObject(response);
                            fileContents.append("{\"Name\":" + "\"" + stockInfo.getString(COMPANY_NAME) + "\"," +
                                    "\"Symbol\":" + "\"" + stockInfo.getString(COMPANY_SYMBOL) + "\"," +
                                    "\"LastPrice\":" + "\"" + stockInfo.getString(COMPANY_STOCK_PRICE) + "\"," +
                                    "\"Chart\":" + "\"" + "http://www.google.com/finance/chart?q=" + stockInfo.getString(COMPANY_SYMBOL) + "&p=1d" + "\"}");
                                    if (i != stockList.length-1){
                                        fileContents.append(",");
                                    }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    fileContents.append("]}");
                    Log.d("File Contents: ", fileContents.toString());
                    outputStream.write(fileContents.toString().getBytes());
                    outputStream.close();
                    parseStockJSON(readStocksFromFile(file));
                } catch (Exception e){
                    e.printStackTrace();
                }
                //send message to main activity
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(FileService.UPDATE_FILE);
                LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
            }
        }
    }

    private JSONObject readStocksFromFile(File file) {

        StringBuilder text = new StringBuilder();
        JSONObject stockJSON = new JSONObject();
        if (file.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            stockJSON = new JSONObject(text.toString());
        } catch (JSONException e) {
            Log.e("Stock Portfolio", "Could not parse JSON object from file :" + text.toString());
        }
        return stockJSON;
    }

    private void parseStockJSON(JSONObject stockJSON){
        try {
            JSONArray stocks = stockJSON.getJSONArray(STOCK_INFO);
            stockList = new Stock[stocks.length()];
            for (int i = 0; i < stocks.length(); i++){
                JSONObject stock = stocks.getJSONObject(i);
                stockList[i] = new Stock(stock.getString(COMPANY_NAME), stock.getString(COMPANY_SYMBOL),
                        stock.getDouble(COMPANY_STOCK_PRICE), stock.getString(COMPANY_STOCK_CHART));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private String getJSONAsString(InputStream input){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input));
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null){
                sb.append(line).append('\n');
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            try {
                input.close();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}


