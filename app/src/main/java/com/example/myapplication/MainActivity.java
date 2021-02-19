package com.example.myapplication;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.Menu;
import android.view.MenuItem;
import android.annotation.SuppressLint;
import android.Manifest;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.Toast;
import android.os.Handler;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    ArrayList<float[]> accelerometerReadings = new ArrayList<>();
    public static final int RECORDING_TIME =  45000;
    float respiratoryRate = 0f;

    @Override
    protected void onResume(){
        super.onResume();

        String number = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("LAST_MEASURE", "0");
        if (number!="0") {

            TextView tv = (TextView) findViewById(R.id.number1);
            tv.setText(number);

            RatingBar rb = (RatingBar) findViewById(R.id.ratingBar);
        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final TextView respiratoryRateView = findViewById(R.id.respiratory_rate);


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
//        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
//        View fg_one = factory.inflate(R.layout.fragment_first, null);
        final Button button = (Button) findViewById(R.id.heartRate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("123","123");
                Intent intent;
                intent = new Intent(getApplicationContext(), HeartMeasure.class);
                startActivity(intent);
            }
        });



        Button measureRespiratoryRate = findViewById(R.id.measure_respiratory_rate);

        measureRespiratoryRate.setOnClickListener(new Button.OnClickListener(){
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {

                Sensor accelerometer;
                final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

                if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

                    final SensorEventListener sensorListener = new SensorEventListener() {

                        public void onSensorChanged(SensorEvent event) {

                            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                                accelerometerReadings.add(new float[]{event.values[0],
                                        event.values[1],
                                        event.values[2]});
                            }
                        }
                        @Override
                        public void onAccuracyChanged(Sensor sensor, int i) {

                        }
                    };

                    respiratoryRateView.setText(" Please wait. Collecting accelerometer data for "
                            + (RECORDING_TIME/1000) + "s...");

                    accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    sensorManager.registerListener(sensorListener,
                            accelerometer,
                            SensorManager.SENSOR_DELAY_NORMAL);

                    new Handler().postDelayed(new Runnable() {

                        @SuppressLint("DefaultLocale")
                        @Override
                        public void run() {

                            sensorManager.unregisterListener(sensorListener);
                            Toast.makeText(getApplicationContext(),
                                    "Accelerometer data collected!",
                                    Toast.LENGTH_LONG)
                                    .show();
                            System.out.println("Accelerometer readings size: "
                                    + accelerometerReadings.size());

                            respiratoryRate = new RespiratoryRateMonitor()
                                    .calculateRespiratoryRate(accelerometerReadings);
                            respiratoryRateView.setText(String.format("%.1f", respiratoryRate));
                        }
                    }, RECORDING_TIME);
                }
                else {

                    Toast.makeText(getApplicationContext(),
                            "No Sensor!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });

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


}