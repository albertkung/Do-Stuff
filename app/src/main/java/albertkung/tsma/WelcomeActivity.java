package albertkung.tsma;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
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

public class WelcomeActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private static String userName = "friend";
    private static String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?";
    private static String appId = "53ec04fa4cf46d6275cfd5a48812e76f";
    private static Location myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getSupportActionBar().setTitle("");

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // time stuff
        Calendar calender = Calendar.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        String formattedTime = timeFormat.format(calender.getTime());
        TextView timeLabel = (TextView) findViewById(R.id.time_label);
        timeLabel.setText(formattedTime);

        // msg stuff
        TextView greetingLabel = (TextView) findViewById(R.id.greeting_label);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getOrder()) {
            case 1: addProfile();
            case 2: editProfiles();
            case 3: changeSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeSettings() {

    }

    private void editProfiles() {

    }

    private void addProfile() {

    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("connected");
        // Once connected, check location to get weather
        mLocationRequest = new LocationRequest();
        mLocationRequest.setNumUpdates(1);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateWeather();
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

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
    public void onLocationChanged(Location location) {
        myLocation = location;
        updateWeather();
    }

    private void updateWeather() {
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
                            JSONObject infoList = response.getJSONArray("list").getJSONObject(0);
                            JSONObject mainInfo = infoList.getJSONObject("main");
                            double currentTemp = mainInfo.getDouble("temp");
                            double currentHumidity = mainInfo.getDouble("humidity");
                            JSONObject weatherInfo = infoList.getJSONArray("weather").getJSONObject(0);
                            int currentConditions = weatherInfo.getInt("id");

                            TextView tempView = (TextView) findViewById(R.id.temperature_label);
                            tempView.setText("" + currentTemp + (char)0x00B0);
                            TextView humidityView = (TextView) findViewById(R.id.humidity_label);
                            humidityView.setText("" + currentHumidity + "%");
                            TextView precipView = (TextView) findViewById(R.id.precip_label);
                            precipView.setText("" + currentConditions);
                            TextView feelsView = (TextView) findViewById(R.id.feels_label);
                            // ?
                            String feels = "";
                            if (currentTemp > 80 && currentHumidity > 50) {
                                feels = "feels hot";
                            }
                            else if (currentTemp < 40) {
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
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(weatherRequest);
        rq.start();
    }
}
