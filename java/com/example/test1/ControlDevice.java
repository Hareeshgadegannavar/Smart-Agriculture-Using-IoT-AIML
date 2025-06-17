package com.example.test1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class ControlDevice extends AppCompatActivity {

    private ImageButton im1,im2,im3,im4,im5,im6;
    private Button b1,b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control_device);
        im1=(ImageButton) findViewById(R.id.moveforward);
        im2=(ImageButton) findViewById(R.id.movebackward);
        im3=(ImageButton) findViewById(R.id.moveleft);
        im4=(ImageButton) findViewById(R.id.moveright);
        im5=(ImageButton) findViewById(R.id.startdevice);
        im6=(ImageButton) findViewById(R.id.stopdevice);
        b1=(Button)findViewById(R.id.srcutter);
        b2=(Button)findViewById(R.id.stcutter);

        im1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("StartCar");
            }
        });
        im2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("Backward");
            }
        });
        im3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("moveLeft");
            }
        });
        im4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("moveRight");
            }
        });
        im5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("StartCar");
            }
        });
        im6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("StopCar");
            }
        });
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("OnCutter");
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendCommand("OffCutter");
            }
        });

    }
    public void sendCommand(final String command) {
        new SendCommandTask().execute(command);
    }

    private static class SendCommandTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... commands) {
            String command = commands[0];
            try {
                // Replace "your-node-mcu-ip" with your NodeMCU's IP address
                URL url = new URL("http://192.168.140.23/command?command=" + command);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.getResponseCode(); // This actually sends the request
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}