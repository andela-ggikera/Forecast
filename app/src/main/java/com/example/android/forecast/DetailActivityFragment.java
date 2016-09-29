package com.example.android.forecast;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.forecast.data.ForecastContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class  DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int DETAIL_LOADER = 0;
    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    public static final String DATE_KEY = "date";
    public static final String LOCATION_KEY = "location";


    private static final String FORECAST_SHARE_HASHTAG = "ForecastApp";
    private String forecastString;

    private String mLocation;
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a
            // table name , since the content provider joins the location
            // & weather tables in the background
            ForecastContract.WeatherEntry.TABLE_NAME + "." + ForecastContract.WeatherEntry._ID,
            ForecastContract.WeatherEntry.COLUMN_DATETEXT,
            ForecastContract.WeatherEntry.COLUMN_SHORT_DESC,
            ForecastContract.WeatherEntry.COLUMN_MAX_TEMP,
            ForecastContract.WeatherEntry.COLUMN_MIN_TEMP,
            ForecastContract.WeatherEntry.COLUMN_HUMIDITY,
            ForecastContract.WeatherEntry.COLUMN_PRESSURE,
            ForecastContract.WeatherEntry.COLUMN_WIND_SPEED,
            ForecastContract.WeatherEntry.COLUMN_DEGREES,
            ForecastContract.WeatherEntry.COLUMN_WEATHER_ID,
            ForecastContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public TextView dateView;
    public TextView descriptionView;
    public TextView highTemperatureView;
    public TextView lowTemperatureView;
    public TextView humidityView;
    public TextView windView;
    public TextView pressureView;


    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // grab the saved location if saved before
        if (null != savedInstanceState) {

        }
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // allow this fragment to handle menu events
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        dateView = (TextView) rootView.findViewById(R.id.detail_date_textview);
        descriptionView = (TextView) rootView.findViewById(R.id.detail_forecast_textview);
        highTemperatureView = (TextView) rootView.findViewById(R.id.detail_high_textview);
        lowTemperatureView = (TextView) rootView.findViewById(R.id.detail_low_textview);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_detail_fragment, menu);

        // Get the shared menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);

        // Get the provider to set the share intent
        ShareActionProvider shareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach intent to this shared action provider
        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(shareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action provider is null");
        }
    }

    private Intent shareForecastIntent () {
        Intent intent = new Intent(Intent.ACTION_SEND);
        // prevent the activity from being placed into the activity stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT,
                forecastString + FORECAST_SHARE_HASHTAG);

        return intent;
    }

    @Override
    public void onSaveInstanceState (Bundle outState) {
        // save the location to our bundle
        super.onSaveInstanceState(outState);
        if (null != mLocation) {
            outState.putString(LOCATION_KEY, mLocation);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (null != mLocation && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent  = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String forecastDate = getActivity().getIntent().getStringExtra(DATE_KEY);

        mLocation = Utility.getPreferredLocation(getActivity());
        // Sort order: Ascending by date
        String sortOrder = ForecastContract.WeatherEntry.COLUMN_DATETEXT + " ASC";

        Uri weatherUri = ForecastContract.WeatherEntry.buildWeatherLocationWithDate(mLocation, forecastDate);

        return new CursorLoader(
                getActivity(),
                weatherUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        String description =
                data.getString(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_SHORT_DESC));
        int weatherId = data.getInt(data.getColumnIndex(
                ForecastContract.WeatherEntry.COLUMN_WEATHER_ID
        ));

        String dateString = Utility.formatDate(
                data.getString(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_DATETEXT))
        );
        boolean isMetric = Utility.isMetric(getActivity());
        String high = Utility.formatTemperature(
                data.getDouble(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_MAX_TEMP)), isMetric
        );

        String low = Utility.formatTemperature(
                data.getDouble(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_MIN_TEMP)), isMetric
        );

        // Read humidity from the cursor and update view
        float humidity = data.getFloat(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_HUMIDITY));

        // Read wind speed and direction
        float windSpeedString = data.getFloat(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_WIND_SPEED));
        float windDirString = data.getFloat(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_DEGREES));

        // Read the pressure from the cursor
        float pressure = data.getFloat(data.getColumnIndex(ForecastContract.WeatherEntry.COLUMN_PRESSURE));

        dateView.setText(dateString);
        highTemperatureView.setText(high + "\u00B0");
        lowTemperatureView.setText(low + "\u00B0");
        descriptionView.setText(description);

        forecastString = String.format("%s - %s - %s/%s",
                dateView.getText(),
                descriptionView.getText(),
                highTemperatureView.getText(),
                lowTemperatureView.getText());


    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
