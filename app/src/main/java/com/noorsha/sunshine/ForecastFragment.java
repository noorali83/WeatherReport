package com.noorsha.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {
    private static final String TAG = "ForecastFragment";
    public final static String EXTRA_MESSAGE = "com.noorsha.sunshine.MESSAGE";

    ArrayAdapter<String> mWeatherDataAdapter;

    public ForecastFragment() {
    }


    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);


        mWeatherDataAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast, R.id.layout_item_forecast_textview, new ArrayList<String>());
        ListView listView = (ListView) view.findViewById(R.id.listView_forecast);
        listView.setAdapter(mWeatherDataAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "position::" + position + " id " + id);
                String item = mWeatherDataAdapter.getItem(position);

                Toast.makeText(getActivity(), item, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(getActivity(), ForecastDetailActivity.class);
                intent.putExtra(EXTRA_MESSAGE, item);
                startActivity(intent);

            }
        });


        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_forecast_fragment, menu);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            Log.d(TAG, "Referesh is clicked");
            updateWeather();
            return true;
        }
        if (item.getItemId() == R.id.action_open_map_location) {
            Log.d(TAG, "Map is clicked");
            //TO DO  Implict intent of opening a location map
            // Create the text message with a string

            String latitude = "1.36667";
            String longtitude = "103.800003";

            Uri location = Uri.parse("geo:" + latitude + "," + longtitude);
            showMap(location);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    public void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void updateWeather() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPref.getString(SettingsActivity.KEY_PREF_LOCATION, "");

        new FetchWeatherDataTask().execute(location);//94043
    }

    public class FetchWeatherDataTask extends AsyncTask<String, Void, String[]> {


        @Override
        protected String[] doInBackground(String... params) {
            Log.d(TAG, "BackGroud task started");
            String weatherDataJsonString = getOpenWeatherData(params[0]);
            String[] weatherDatasFromJson = new String[0];
            try {
                weatherDatasFromJson = WeatherDataJsonDataExtractor.getWeatherDataFromJson(weatherDataJsonString, 7, getActivity());
                for (String weatherData : weatherDatasFromJson) {
                    Log.d(TAG, weatherData);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return weatherDatasFromJson;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p/>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param strings The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            mWeatherDataAdapter.clear();
            mWeatherDataAdapter.addAll(strings);
        }
    }

    private String getOpenWeatherData(String param) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String forecastJsonStr = null;

        try {
            // Construct the URL for the OpenWeatherMap query
            // Possible parameters are avaiable at OWM's forecast API page, at
            // http://openweathermap.org/API#forecast
            //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?id=1880251&cnt=7&units=metric&&APPID=7c9e0ccc3b88ff16b00f8016a01a7e2c");

            final String FORECAST_BASE_URL =
                    "http://api.openweathermap.org/data/2.5/forecast/daily?";
            final String QUERY_PARAM = "id";
            final String FORMAT_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String DAYS_PARAM = "cnt";
            final String APP_ID = "APPID";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(QUERY_PARAM, param)
                    .appendQueryParameter(FORMAT_PARAM, "json")
                    .appendQueryParameter(UNITS_PARAM, "metric")
                    .appendQueryParameter(DAYS_PARAM, Integer.toString(7))
                    .appendQueryParameter(APP_ID, "7c9e0ccc3b88ff16b00f8016a01a7e2c")
                    .build();

            URL url = new URL(builtUri.toString());

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                forecastJsonStr = null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                forecastJsonStr = null;
            }
            forecastJsonStr = buffer.toString();
            Log.d(TAG, "forecastJsonStr" + forecastJsonStr);
        } catch (IOException e) {
            Log.e("PlaceholderFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            forecastJsonStr = null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }
        return forecastJsonStr;

    }
}
