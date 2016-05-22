package com.noorsha.sunshine;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastDetailActivityFragment extends Fragment {

    public ForecastDetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast_detail, container, false);
        String detailMessage = getActivity().getIntent().getExtras().getString("com.noorsha.sunshine.MESSAGE");
        TextView textView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        textView.setText(detailMessage);
        return view;
    }
}
