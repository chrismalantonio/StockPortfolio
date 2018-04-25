package edu.temple.stockportfolio;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class NavFragment extends Fragment {

    private static final String COMPANY_LIST_LENGTH = "Number of Companies";
    StockSelectionInterface parent;

    public NavFragment() {
        // Required empty public constructor
    }

    public static NavFragment newInstance(Stock[] stockList) {
        NavFragment fragment = new NavFragment();
        Bundle args = new Bundle();
        if (stockList != null) {
            for (int i = 0; i < stockList.length; i++) {
                Log.d("For loop", "Value of i: "+ i);
                args.putString("stock" + i, stockList[i].getName() + " (" + stockList[i].getSymbol() + ")");
            }
            args.putInt(COMPANY_LIST_LENGTH, stockList.length);
        }

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        parent = (StockSelectionInterface) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_nav, container, false);
        Bundle args = getArguments();

        ListView stockList = rootView.findViewById(R.id.stockList);
        stockList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                parent.viewStockDetails(i);
            }
        });

        //if file exists, don't tell users to add stocks
        TextView notifyUser = (TextView) rootView.findViewById(R.id.notifyUser);
        if (!args.isEmpty()){
            notifyUser.setVisibility(TextView.GONE);
            //populate list view with company names
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, getCompanyNames());
            stockList.setAdapter(adapter);
        } else {
            notifyUser.setVisibility(TextView.VISIBLE);
        }

        return rootView;
    }

    private ArrayList<String> getCompanyNames(){
        ArrayList<String> companyList = new ArrayList<String>();
        Bundle args = getArguments();
        int length = args.getInt(COMPANY_LIST_LENGTH);
        for (int i = 0; i < args.getInt(COMPANY_LIST_LENGTH); i++){
            companyList.add(args.getString("stock" + i));
        }

        return companyList;
    }

    public interface StockSelectionInterface {
        void viewStockDetails(int position);
    }
}
