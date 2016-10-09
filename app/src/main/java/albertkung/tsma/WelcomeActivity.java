package albertkung.tsma;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.Calendar;

public class WelcomeActivity extends AppCompatActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private TaskManager manager;

    private static final int ADD_REQUEST = 69;

    private static String userName = "friend";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getSupportActionBar().setTitle("");

        // api client for getting location
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // msg stuff
        Calendar calender = Calendar.getInstance();
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

        // initialize weather stuff
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/weathericons.ttf");
        ((TextView) findViewById(R.id.weather_icon)).setTypeface(type);
        ((TextView) findViewById(R.id.precip_label)).setTypeface(type);

        // get tasks
        manager = new TaskManager();
        manager.restoreTasks(this);
        updateTasks();
    }

    private void updateTasks() {
        TextView task_badge = (TextView) findViewById(R.id.task_badge);
        if (task_badge != null) {
            task_badge.setText("" + manager.getNumTasks(1));
        }
    }

    public void showTasks(View view) {
        if (view.getId() == R.id.today_tasks) {
            Intent intent = new Intent(this, DisplayActivity.class);
            startActivity(intent);
        }
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
            case 1: addActivity();
            case 2: editProfiles();
            case 3: changeSettings();
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeSettings() {

    }

    private void editProfiles() {

    }

    private void addActivity() {
        Intent intent = new Intent(this, AddActivity.class);
        startActivityForResult(intent, ADD_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ADD_REQUEST && resultCode == Activity.RESULT_OK) {
            Task result = (Task) data.getSerializableExtra("task");
            manager.addTask(result);
            updateTasks();
        }
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
        // check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        // check for location enabled
        else if (!isLocationEnabled(this)) {
            Toast.makeText(this, "Enable location to receive weather information.", Toast.LENGTH_LONG).show();
        }
        // update weather
        else {
            updateWeather(LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient));
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
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
    public void onPause() {
        super.onPause();
        manager.saveTasks(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        updateWeather(location);
    }

    private void updateWeather(Location location) {
        String weatherUrl = "http://api.openweathermap.org/data/2.5/forecast?";
        String appId = "53ec04fa4cf46d6275cfd5a48812e76f";
        String myUrl = weatherUrl
                + "lat=" + location.getLatitude()
                + "&lon=" + location.getLongitude()
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
                            int currentHumidity = (int) mainInfo.getDouble("humidity");
                            JSONObject weatherInfo = infoList.getJSONArray("weather").getJSONObject(0);
                            int currentConditions = weatherInfo.getInt("id");

                            TextView tempView = (TextView) findViewById(R.id.temperature_label);
                            tempView.setText("" + currentTemp + (char)0x00B0);
                            TextView humidityView = (TextView) findViewById(R.id.humidity_label);
                            humidityView.setText(currentHumidity + " ");
                            ((TextView) findViewById(R.id.precip_label)).setText(Html.fromHtml("&#xf07a"));
                            TextView feelsView = (TextView) findViewById(R.id.feels_label);
                            // ?
                            String feels = "";
                            if (currentTemp > 90 && currentHumidity > 60) {
                                feels = "feels mad hot";
                            }
                            else if (currentTemp > 80 && currentHumidity > 50) {
                                feels = "feels hot";
                            }
                            else if (currentTemp < 20) {
                                feels = "feels mad cold";
                            }
                            else if (currentTemp < 40) {
                                feels = "feels cold";
                            }
                            else {
                                feels = "feels good";
                            }
                            feelsView.setText(feels);

                            // weather icon
                            String weather_code;
                            if (currentConditions < 300) {
                                weather_code = "f005";
                            }
                            else if (currentConditions < 400) {
                                weather_code = "f009";
                            }
                            else if (currentConditions < 600) {
                                weather_code = "f008";
                            }
                            else if (currentConditions < 700) {
                                weather_code = "f065";
                            }
                            else if (currentConditions == 800) {
                                weather_code = "f00d";
                            }
                            else if (currentConditions < 900) {
                                weather_code = "f002";
                            }
                            else {
                                weather_code = "f075";
                            }
                            TextView weather_icon = ((TextView) findViewById(R.id.weather_icon));
                            weather_icon.setText(Html.fromHtml("&#x" + weather_code));

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
