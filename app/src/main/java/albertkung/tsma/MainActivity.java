package albertkung.tsma;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    private GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;

    private static String userName = "friend";
    private static String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?";
    private static String appId = "53ec04fa4cf46d6275cfd5a48812e76f";
    private static Location myLocation;

    private static Context myContext; // for cheap hacks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        myContext = getApplicationContext();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Stuff for location services. */
    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            LocationServices.FusedLocationApi.
                    requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /* Stuff for location services. */
    @Override
    public void onConnectionSuspended(int i) {

    }

    /* Stuff for location services. */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 0 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // put this here so no error
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            LocationServices.FusedLocationApi.
                    requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /* Called upon location change to set the weather on the main page. */
    @Override
    public void onLocationChanged(Location location) {
        myLocation = location;
        PlaceholderFragment fragment = (PlaceholderFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_main);
        if (fragment != null) {
            fragment.changeWeather();
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onResume() {
            super.onResume();
            if (myLocation != null) {
                changeWeather();
            }
            else {
                // placeholder until weather is updated
                TextView v = (TextView) getView().findViewById(R.id.temperature_label);
                v.setText("Checking weather patterns...");
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setTime(rootView);
            return rootView;
        }

        private void setTime(View root) {
            // time stuff
            Calendar calender = Calendar.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
            String formattedTime = timeFormat.format(calender.getTime());
            TextView timeLabel = (TextView) root.findViewById(R.id.time_label);
            timeLabel.setText(formattedTime);

            // msg stuff
            TextView greetingLabel = (TextView) root.findViewById(R.id.greeting_label);
            String greeting;
            int currentTime = calender.get(Calendar.HOUR_OF_DAY);
            if (currentTime < 12) {
                greeting = "Good morning";
            }
            else if (currentTime < 17) {
                greeting = "Good afternoon";
            }
            else {
                greeting = "Good evening";
            }
            greetingLabel.setText(greeting + ", " + userName);
        }

        private void changeWeather() {
            String myUrl = weatherUrl
                    + "lat=" + myLocation.getLatitude()
                    + "&lon=" + myLocation.getLongitude()
                    + "&units=imperial"
                    + "&appid=" + appId;
            JsonObjectRequest weatherRequest = new JsonObjectRequest
                    (Request.Method.GET, myUrl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                View root = getView();
                                if (root == null) return; // sanity check

                                JSONObject infoList = response.getJSONArray("list").getJSONObject(0);
                                JSONObject mainInfo = infoList.getJSONObject("main");
                                double currentTemp = mainInfo.getDouble("temp");
                                double currentHumidity = mainInfo.getDouble("humidity");
                                JSONObject weatherInfo = infoList.getJSONArray("weather").getJSONObject(0);
                                int currentConditions = weatherInfo.getInt("id");

                                TextView tempView = (TextView) root.findViewById(R.id.temperature_label);
                                tempView.setText("" + currentTemp + (char)0x00B0);
                                TextView humidityView = (TextView) root.findViewById(R.id.humidity_label);
                                humidityView.setText("" + currentHumidity + "%");
                                TextView precipView = (TextView) root.findViewById(R.id.precip_label);
                                precipView.setText(currentConditions);
                                TextView feelsView = (TextView) root.findViewById(R.id.humidity_label);
                                // ?
                                String feels = "";
                                if (currentTemp > 90 && currentHumidity > 50) {
                                    feels = "feels hot";
                                }
                                else if (currentTemp < 30) {
                                    feels = "feels cold";
                                }
                                else {
                                    feels = "feels good";
                                }
                                feelsView.setText(feels);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // weather failed! :(
                        }
                    });
            RequestQueue rq = Volley.newRequestQueue(myContext);
            rq.add(weatherRequest);
            rq.start();

        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
