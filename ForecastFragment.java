package com.example.android.sunshine.app;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.ListView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;

import com.example.android.sunshine.R;
import com.example.android.sunshine.app.data.WeatherContract;

import java.util.Calendar;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    // TODO: Rename parameter arguments, choose names that match
    private static final int FORECAST_LOADER = 0;
    Calendar calendar = Calendar.getInstance();
    long sekarang;

    private ForecastAdapter mForecastAdapter;

    private static final String[] FORECAST_COLUMNS ={
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX = 3;
    static final int COL_WEATHER_MIN = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;


    public ForecastFragment() {
    }

    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String locationSetting = Utility.getPreferredLocation(getActivity());
        sekarang = calendar.getTimeInMillis();

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + "  ASC ";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, sekarang);
        Log.d(LOG_TAG, "Uri yang terbentuk = "+weatherForLocationUri.toString());
        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                FORECAST_COLUMNS, null, null, sortOrder);
        mForecastAdapter = new ForecastAdapter(getActivity(), cur, 0);

        View rootView = inflater.inflate(R.layout.fragment_forecast, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.list_forecast);
        //Log.d("ForecastFragment", "sampe dapetin listview");
        listView.setAdapter(mForecastAdapter);
        //Log.d("ForecastFragment", "sampe ngeset adapter");

        //supaya bisa diklik
        listView.setOnItemClickListener(
            new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    // CursorAdapter returns a cursor at the correct position for getItem(), or null
                    // if it cannot seek to that position.
                    Cursor cursor = (Cursor) adapterView.getItemAtPosition(i);
                    if (cursor != null) {
                        String locationSetting = Utility.getPreferredLocation(getActivity());
                        Intent intent = new Intent(getActivity(), DetailAct.class)
                                .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                        locationSetting, cursor.getLong(COL_WEATHER_DATE)
                                ));
                        startActivity(intent);
                    }
                }
            }
        );

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
//            Toast toast = Toast.makeText(getActivity(), "Seharusnya ud update", Toast.LENGTH_SHORT);
//            toast.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private void isiArrayList(String [] data, String unitType){
//        itemsAdapter.clear();
//        for(int i=0; i<data.length; i++) {
//            String[] isi = data[i].split(" - ");
//            String hari, predik,hi, lo;
//            double high, low;
//
//            hari = isi[0];
//            predik = isi[1];
//            String [] hilo = isi[2].split("/");
//            hi=hilo[0];
//            high = Integer.parseInt(hi);
//            lo = hilo[1];
//            low = Integer.parseInt(lo);
//
//            if (unitType.equals(getString(R.string.pref_units_imperial))) {
//                high = (high * 1.8) + 32;
//                low = (low * 1.8) + 32;
//            } else if (!unitType.equals(getString(R.string.pref_units_metric))) {
//                Log.d(LOG_TAG, "Unit type not found: " + unitType);
//            }
//            long roundedHigh = Math.round(high);
//            long roundedLow = Math.round(low);
////            cuacas.add(new Prediksi_cuaca(hari,predik, Integer.parseInt(hi), Integer.parseInt(lo), R.drawable.sunny));
//            itemsAdapter.add(new Prediksi_cuaca(hari,predik, roundedHigh, roundedLow, R.drawable.sunny));
//        }
//
////        itemsAdapter = new list_forecast_adapter(getActivity(), cuacas);
//    }

    public void updateWeather(){
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    void onLocationChanged( ) {
        updateWeather();
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        //sort order asc by date
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                locationSetting, sekarang);

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
