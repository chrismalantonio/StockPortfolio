package edu.temple.stockportfolio;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.DialogInterface;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity implements NavFragment.StockSelectionInterface {

    private static final String STOCK_INFO = "StockList";
    private static final String COMPANY_NAME = "Name";
    private static final String COMPANY_SYMBOL = "Symbol";
    private static final String COMPANY_STOCK_PRICE = "LastPrice";
    private static final String COMPANY_STOCK_CHART = "Chart";

    private String internalFileName = "StockPortfolioFile";
    private File file;
    private FragmentManager fm;
    private DetailsFragment detailsFragment;
    private Stock[] stockList;
    private int detailsIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        file = new File(getFilesDir(), internalFileName);

        //get information from file on application launch

        JSONObject stockJSON = readStocksFromFile(file);
        if (stockJSON.length() != 0) {
            parseStockJSON(stockJSON);
        }

        fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.navContainer, NavFragment.newInstance(stockList)).commit();
        if (findViewById(R.id.detailsContainer) != null) {
            detailsFragment = DetailsFragment.newInstance(new Stock("", "", 0, ""));
            fm.beginTransaction().replace(R.id.detailsContainer, detailsFragment).commit();
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter(FileService.UPDATE_FILE));
        Intent intent = new Intent(this, FileService.class);
        intent.setClass(this, FileService.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction(FileService.UPDATE_FILE);
        registerReceiver(broadcastReceiver, filter);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Update stock objects
            Log.d("onReceive", "We received the broadcast.");
            JSONObject stockJSON = readStocksFromFile(file);
            if (stockJSON.length() != 0) {
                parseStockJSON(stockJSON);
            }

            //update fragments with new stock information
            if (findViewById(R.id.detailsContainer) != null && detailsIndex != -1) {
                fm.beginTransaction().replace(R.id.navContainer, NavFragment.newInstance(stockList)).commit();
                viewStockDetails(detailsIndex);
            } else {
                Fragment currentFragment = fm.findFragmentById(R.id.navContainer);
                if (currentFragment instanceof NavFragment) {
                    fm.beginTransaction().replace(R.id.navContainer, NavFragment.newInstance(stockList)).commit();
                } else {
                    fm.popBackStack();
                    viewStockDetails(detailsIndex);
                }
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newButton:
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Add to Stock Portfolio");
                alert.setMessage("Enter a company's stock symbol:");
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    new Thread(){
                      @Override
                      public void run(){
                          try {
                              FileOutputStream outputStream = new FileOutputStream(file);
                              StringBuffer fileContents = new StringBuffer("{\"StockList\":[");
                              String symbol = input.getText().toString();
                              String stockAPI = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol;
                              String response;
                              try {
                                  URL url = new URL(stockAPI);
                                  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                  conn.setRequestMethod("GET");
                                  InputStream input = new BufferedInputStream(conn.getInputStream());
                                  response = getJSONAsString(input);
                                  JSONObject stockInfo = new JSONObject(response);
                                  Log.d("Name: ", stockInfo.getString(COMPANY_NAME));
                                  Log.d("Symbol: ", stockInfo.getString(COMPANY_SYMBOL));
                                  Log.d("LastPrice: ", stockInfo.getString(COMPANY_STOCK_PRICE));
                                  if (stockList != null) {
                                      for (int i = 0; i < stockList.length; i++) {
                                          fileContents.append("{\"Name\":" + "\"" + stockList[i].getName() + "\"," +
                                                  "\"Symbol\":" + "\"" + stockList[i].getSymbol() + "\"," +
                                                  "\"LastPrice\":" + "\"" + stockList[i].getPrice() + "\"," +
                                                  "\"Chart\":" + "\"" + stockList[i].getChart() + "\"},");
                                      }
                                  }
                                  fileContents.append("{\"Name\":" + "\"" + stockInfo.getString(COMPANY_NAME) + "\"," +
                                          "\"Symbol\":" + "\"" + stockInfo.getString(COMPANY_SYMBOL) + "\"," +
                                          "\"LastPrice\":" + "\"" + stockInfo.getString(COMPANY_STOCK_PRICE) + "\"," +
                                          "\"Chart\":" + "\"" + "http://www.google.com/finance/chart?q=" + stockInfo.getString(COMPANY_SYMBOL) +"&p=1d" + "\"}");

                              } catch (Exception e){
                                  e.printStackTrace();
                              }

                              fileContents.append("]}");
                              Log.d("File Contents: ", fileContents.toString());
                              outputStream.write(fileContents.toString().getBytes());
                              outputStream.close();
                              parseStockJSON(readStocksFromFile(file));
                              fm.beginTransaction().replace(R.id.navContainer, NavFragment.newInstance(stockList)).addToBackStack(null).commit();
                          } catch (Exception e){
                              e.printStackTrace();
                          }
                      }
                    }.start();
                        try {
                            Toast.makeText(MainActivity.this, "Added new company: " + input.getText(), Toast.LENGTH_SHORT).show();
                            findViewById(R.id.notifyUser).setVisibility(TextView.GONE);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Do nothing
                    }
                });

                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public JSONObject readStocksFromFile(File file) {

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

    @Override
    public void viewStockDetails(int position) {
        Log.d("viewStockDetails onClick", "This should send you to the Details page.");
        if (findViewById(R.id.detailsContainer) != null) {
            detailsFragment.loadStockDetails(stockList[position]);
        } else {
            try {
                Log.d("viewStockDetails onClick", "in else statement");
                detailsFragment = DetailsFragment.newInstance(stockList[position]);
                fm.beginTransaction().replace(R.id.navContainer, detailsFragment).addToBackStack(null).commit();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        detailsIndex = position;
    }

    //Populating the stock list with stock objects from the file
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


//    private void writeToFile(){
//        try {
//            FileOutputStream outputStream = new FileOutputStream(file);
//            String fileContents = "{\"StockList\":[{\"Name\":\"Google\",\"Symbol\":\"GOOG\",\"LastPrice\":\"1089.91\", \"Chart\":\"https://www.google.com/finance/chart?q=goog&p=1d\"}]}";
//            outputStream.write(fileContents.getBytes());
//            outputStream.close();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//
//    }
}
