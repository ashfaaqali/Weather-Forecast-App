package com.example.weatherforecastapp.cardview1;
import static com.example.weatherforecastapp.MainActivity.appId;
import static com.example.weatherforecastapp.MainActivity.weatherURL;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class CardView1Weather extends AppCompatActivity {
    TextView cv1City, cv1weatherType, cv1Date, cv1Temp, cv1WindSpeed, cv1Humidity, cv1Description;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private void getWeatherUpdates() {
        String tempURL = weatherURL + "?q=Kolkata&appid=" + appId;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, tempURL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("response2", response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(CardView1Weather.this, error.toString().trim(), Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }
    }