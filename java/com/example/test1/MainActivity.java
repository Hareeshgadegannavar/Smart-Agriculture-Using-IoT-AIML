package com.example.test1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOCATION_PERMISSION = 1234;
    private TextView t1, t2, t3;
    private Button b1, b2, b3;

    private final int REFRESH_DELAY_1 = 20000; // Refresh every 20 seconds
    private final int REFRESH_DELAY_2 = 30000; // Refresh every 30 seconds
    private Handler handler1, handler2;
    private String temp, humi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        t1 = findViewById(R.id.field1);
        t2 = findViewById(R.id.field2);
        t3 = findViewById(R.id.field3);
        b1 = findViewById(R.id.predict);
        b2 = findViewById(R.id.spredict);
        b3 = findViewById(R.id.cdevice);

        // Initialize FusedLocationProviderClient


        // Initialize Handler
        handler1 = new Handler();
        handler2 = new Handler();

        // Button click listeners
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start PredictCrop activity
                startActivity(new Intent(getApplicationContext(), PredictCrop.class));
            }
        });

        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start SensorPredict activity
                navigateToOtherActivity();
            }
        });

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start ControlDevice activity
                startActivity(new Intent(getApplicationContext(), ControlDevice.class));
            }
        });

        // Fetch data from ThingSpeak

        fetchThingSpeakData_1();

        //new CheckNodeMCUConnection().execute();

    }

    private void stopDataRefresh() {
        handler1.removeCallbacksAndMessages(null); // Remove any pending callbacks
        handler2.removeCallbacksAndMessages(null); // Remove any pending callbacks
    }

    private void navigateToOtherActivity() {
        stopDataRefresh(); // Stop data refresh
        Intent intent = new Intent(MainActivity.this, SensorPredict.class);
        intent.putExtra("temp", temp);
        intent.putExtra("humidity", humi);
        startActivity(intent);
    }

    private void fetchThingSpeakData_1() {
        // Specify the number of results to fetch
        int resultsCount = 3; // Fetch the last three entries

        // Execute AsyncTask to fetch data from ThingSpeak
        new FetchThingSpeakData_1(resultsCount).execute();

        // Schedule data refresh every 20 seconds
        handler1.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchThingSpeakData_1(); // Fetch data again after 20 seconds
            }
        }, REFRESH_DELAY_1);
    }


    private class FetchThingSpeakData_1 extends AsyncTask<Void, Void, String> {

        private int resultsCount;

        // Constructor to specify the number of results to fetch
        public FetchThingSpeakData_1(int resultsCount) {
            this.resultsCount = resultsCount;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                String url = "https://api.thingspeak.com/channels/2444941/feeds.json?api_key=GJUQP02FUIDC4BAI&results=" + resultsCount;

                HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }

                bufferedReader.close();
                return stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray feedsArray = jsonObject.getJSONArray("feeds");
                    if (feedsArray.length() > 2) { // Ensure there are at least three entries

                        JSONObject obj2 = feedsArray.getJSONObject(0);
                        String soil = obj2.getString("field1");
                        temp = obj2.getString("field2");
                        humi = obj2.getString("field3");
                        t1.setText(soil);
                        t2.setText(temp);
                        t3.setText(humi);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class CheckNodeMCUConnection extends AsyncTask<Void, Void, Boolean> {

        private static final String NODEMCU_URL = "http://192.168.140.23"; // Replace with your NodeMCU IP address

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                URL url = new URL(NODEMCU_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000); // Adjust timeout as needed
                connection.connect();
                int responseCode = connection.getResponseCode();

                return responseCode == HttpURLConnection.HTTP_OK;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            if (isConnected) {
                // If NodeMCU is connected, upload data to ThingSpeak

            } else {
                // If NodeMCU is not connected, do not upload data to ThingSpeak
                // Handle this scenario based on your requirements
                // For example, you can display a message indicating that the NodeMCU is not connected
                // Or you can simply skip the data upload process
                // For now, we'll just log a message indicating that data upload is skipped
                Log.d("CheckNodeMCUConnection", "NodeMCU is not connected. Skipping data upload.");
            }
        }
    }



}
