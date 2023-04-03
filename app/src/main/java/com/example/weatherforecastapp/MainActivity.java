package com.example.weatherforecastapp;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationRequest locationRequest;


    public static final String appId = "7e5cbbcc16342fda4aa4d7a37f53173d";
    public static final String weatherURL = "https://api.openweathermap.org/data/2.5/weather";
    final long MIN_TIME = 5000;
    final float MIN_DISTANCE = 1000;
    private final static int REQUEST_CODE = 100;

    String LocationProvider = LocationManager.GPS_PROVIDER;

    LocationManager locationManager;
    LocationListener locationListener;

    TextView currentLocationName, currentTemp, currentWeatherType, currentDayDate, currentWindSpeed, CurrentHumidity, CurrentDescription;

    DecimalFormat df = new DecimalFormat("#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        currentLocationName = findViewById(R.id.current_location_name);
        currentTemp = findViewById(R.id.current_temp);
        currentWeatherType = findViewById(R.id.current_weather_type);
        currentDayDate = findViewById(R.id.current_day_date);
        currentWindSpeed = findViewById(R.id.current_wind_speed);
        CurrentHumidity = findViewById(R.id.current_humidity);
        CurrentDescription = findViewById(R.id.current_description);

        LocalDate currentdate = null;
        DateTimeFormatter dtf = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentdate = LocalDate.now();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            currentDayDate.setText(String.valueOf(dtf.format(currentdate)));
        }

        SharedPreferences json = getSharedPreferences("weatherJSON", MODE_PRIVATE);
        SharedPreferences.Editor spEd = json.edit();

        if(isNetworkAvailable()){
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);

            //FOR OS VERSIONS GREATER THAN MARSHMALLOW
            //BECAUSE PERMISSIONS ARE AUTO-GRANTED TILL OS VERSION MARSHMALLOW

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    if (isGPSEnabled()) {
                        LocationServices.getFusedLocationProviderClient(MainActivity.this).requestLocationUpdates(locationRequest, new LocationCallback() {

                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
                                super.onLocationResult(locationResult);
                                LocationServices.getFusedLocationProviderClient(MainActivity.this).removeLocationUpdates(this);

                                if (locationResult != null && locationResult.getLocations().size() > 0) {

                                    int index = locationResult.getLocations().size() - 1;
                                    double latitude = locationResult.getLocations().get(index).getLatitude();
                                    double longitude = locationResult.getLocations().get(index).getLongitude();

                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                    try {
                                        List<Address> adresses = geocoder.getFromLocation(latitude, longitude, index);
                                        currentLocationName.setText(adresses.get(0).getLocality());
                                        String city = adresses.get(0).getLocality().toString();

                                        //SAVING CURRENT LOCATION
                                        spEd.putString("currentLocation", city);

                                        String tempURL = weatherURL + "?q=" + city + "&appid=" + appId;

                                        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempURL, new Response.Listener<String>() {
                                            @Override
                                            public void onResponse(String response) {
//                                            Log.d("response", response);

                                                try {


//                                                if (json.contains("weatherURL")){
//                                                    String storedValue = json.getString("weatherJSON", "");
//                                                    sub1edit.setText(storedValue);
//                                                    if (storedValue.length()>0) {sub1edit.setEnabled(false);}
//                                                }

                                                    JSONObject jsonResponse = new JSONObject(response);

                                                    JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                                                    JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                                                    String description = jsonObjectWeather.getString("description");
                                                    String type = jsonObjectWeather.getString("main");
                                                    JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                                                    double temp = jsonObjectMain.getDouble("temp") - 273.15;
                                                    int roundedInt = Integer.parseInt(df.format(temp));
                                                    int humidity = jsonObjectMain.getInt("humidity");
                                                    JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                                                    String wind = jsonObjectWind.getString("speed");
                                                    JSONObject jsonObjectCloud = jsonResponse.getJSONObject("clouds");
                                                    String clouds = jsonObjectCloud.getString("all");


                                                    //SAVING RESPONSE IN SHARED PREFERENCES

                                                    spEd.putString("description", description);
                                                    spEd.putString("temp", String.valueOf(roundedInt));
                                                    spEd.putString("main", type);
                                                    spEd.putString("wind", wind);
                                                    spEd.putString("humidity", String.valueOf(humidity));
                                                    spEd.apply();

                                                    currentTemp.setText(String.valueOf(roundedInt + "°C"));
                                                    currentWindSpeed.setText(String.valueOf(wind + " m/s"));
                                                    CurrentHumidity.setText(String.valueOf(humidity + " %"));
                                                    CurrentDescription.setText(description);
                                                    currentWeatherType.setText(type);



                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                            }
                                        }, new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {
                                                Toast.makeText(MainActivity.this, error.toString().trim(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                                        requestQueue.add(stringRequest);

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }, Looper.getMainLooper());
                    } else {
                        turnOnGPS();
                    }
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                }
            }
        } else {
//            spEd.putString("description", description);
//            spEd.putString("temp", String.valueOf(roundedInt));
//            spEd.putString("main", type);
//            spEd.putString("wind", wind);
//            spEd.putString("humidity", String.valueOf(humidity));

            if(json.contains("currentLocation")){
                String storedVal = json.getString("currentLocation", "");
                currentLocationName.setText(String.valueOf(storedVal));
            }
            if(json.contains("temp")){
                String storedVal = json.getString("temp", "");
                currentTemp.setText(String.valueOf(storedVal));
            }
            if(json.contains("main")){
                String storedVal = json.getString("main", "");
                currentWeatherType.setText(String.valueOf(storedVal));
            }
            if(json.contains("wind")){
                String storedVal = json.getString("wind", "");
                currentWindSpeed.setText(String.valueOf(storedVal));
            }
            if(json.contains("humidity")){
                String storedVal = json.getString("humidity", "");
                CurrentHumidity.setText(String.valueOf(storedVal));
            }
            if(json.contains("description")){
                String storedVal = json.getString("description", "");
                CurrentDescription.setText(String.valueOf(storedVal));
            }
        }
        }



//  END OF ON CREATE METHOD     ======================================================================================================================================================================================================================

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void turnOnGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS is already tured on", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException)e;
                                resolvableApiException.startResolutionForResult(MainActivity.this,2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isGPSEnabled(){
        LocationManager locationManager = null;
        boolean isEnabled = false;
        if (locationManager==null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return isEnabled;
    }
}