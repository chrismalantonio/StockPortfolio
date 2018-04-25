package edu.temple.stockportfolio;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DetailsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DetailsFragment extends Fragment {
    private static final String NAME_KEY = "Name key";
    private static final String CHART_KEY = "Chart key";
    private static final String PRICE_KEY = "Price key";

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(Stock stock) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(NAME_KEY, stock.getName());
        args.putString(CHART_KEY, stock.getChart());
        args.putDouble(PRICE_KEY, stock.getPrice());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);
        TextView companyName = rootView.findViewById(R.id.companyName);
        TextView stockPrice = rootView.findViewById(R.id.stockPrice);
        ImageView chart = rootView.findViewById(R.id.chart);
        Bundle args = getArguments();

        if (!args.isEmpty()){
            try {
                companyName.setText(args.getString(NAME_KEY));
                stockPrice.setText("$" + Double.toString(args.getDouble(PRICE_KEY)));
                Picasso.with(chart.getContext()).load(args.getString(CHART_KEY)).into(chart);
            } catch (Exception e){
                Log.d("DetailsFragment", "Could not set element views on DetailsFragment.");
            }
        }

        return rootView;
    }

    public void loadStockDetails(Stock stock){
        TextView companyName = getView().findViewById(R.id.companyName);
        TextView stockPrice = getView().findViewById(R.id.stockPrice);
        ImageView chart = getView().findViewById(R.id.chart);
        try {
            companyName.setText(stock.getName());
            stockPrice.setText("$"+Double.toString(stock.getPrice()));
            Picasso.with(chart.getContext()).load(stock.getChart()).into(chart);
        } catch (Exception e){
            Log.d("loadStockDetails", "Stock details failed to load.");
        }
    }
}
