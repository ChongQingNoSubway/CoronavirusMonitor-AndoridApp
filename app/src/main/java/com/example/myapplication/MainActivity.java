package com.example.myapplication;

import android.content.SharedPreferences;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
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
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.content.Context;
import android.widget.Toast;
import android.os.Handler;

import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.util.dataBaseSort;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    ArrayList<float[]> accelerometerReadings = new ArrayList<>();
    public static final int RECORDING_TIME =  45000;
    float respiratoryRate = 0f;

    SurfaceHolder SurfaceHolder;
    android.view.SurfaceView SurfaceView;
    public Camera mCamera;
    boolean mPreviewRunning;

    private static int averageIndex = 0;
    private static final int averageArraySize = 4;
    private static final int[] averageArray = new int[averageArraySize];
    public static Context c;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    String date=format.format(new Date());
    private static final AtomicBoolean processing = new AtomicBoolean(false);
    public  static TextView ts = null;
    public  static TextView tip = null;
    private static double beats = 0;
    private static long startTime = 0;
    private static int beatsIndex = 0;
    private static final int beatsArraySize = 3;
    private static final int[] beatsArray = new int[beatsArraySize];
    private boolean isFinished = false;
    private static int heartRate = 0;
    public enum TYPE {
        GREEN, RED
    };
    private static String username = "wenhui";
    private static HeartMeasure.TYPE currentType = HeartMeasure.TYPE.GREEN;

    public static HeartMeasure.TYPE getCurrent() {
        return currentType;
    }

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
    protected void onDestroy() {
        super.onDestroy();
        SurfaceView.getHolder().removeCallback(this);
    }

    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            SurfaceView.getHolder().removeCallback(this);
            mCamera.release();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        Camera.Parameters parameters = mCamera.getParameters();
        if(parameters.getMaxExposureCompensation() != parameters.getMinExposureCompensation()){
            parameters.setExposureCompensation(0);
        }
        mCamera.setParameters(parameters);
        mCamera.setPreviewCallback(previewCallback);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3){
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(arg0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setCameraDisplayOrientation(mCamera);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
    }


    public void setCameraDisplayOrientation(android.hardware.Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        android.hardware.Camera.CameraInfo camInfo = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, camInfo);

        Display display = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * FROM: https://github.com/phishman3579/android-heart-rate-monitor
     */
    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {


        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (mPreviewRunning == false) {
                return;
            }
            if (data == null) throw new NullPointerException();
            Camera.Size size = camera.getParameters().getPreviewSize();
            if (size == null) throw new NullPointerException();

            if (!processing.compareAndSet(false, true)) return;

            int width = size.width;
            int height = size.height;

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(), height, width);
            // Log.i(TAG, "imgAvg="+imgAvg);
            if (imgAvg == 0 || imgAvg == 255) {
                processing.set(false);
                return;
            }

            int averageArrayAvg = 0;
            int averageArrayCnt = 0;
            for (int i = 0; i < averageArray.length; i++) {
                if (averageArray[i] > 0) {
                    averageArrayAvg += averageArray[i];
                    averageArrayCnt++;
                }
            }

            int rollingAverage = (averageArrayCnt > 0) ? (averageArrayAvg / averageArrayCnt) : 0;

            HeartMeasure.TYPE newType = currentType;

            if (imgAvg < rollingAverage) {
                newType = HeartMeasure.TYPE.RED;
                if (newType != currentType) {
                    beats++;
                    // Log.d(TAG, "BEAT!! beats="+beats);
                }
            } else if (imgAvg > rollingAverage) {
                newType = HeartMeasure.TYPE.GREEN;
            }

            if (averageIndex == averageArraySize) averageIndex = 0;
            averageArray[averageIndex] = imgAvg;
            averageIndex++;

            ts.setText("please wait 45S , and put your finger on the back camera");
            long endTime = System.currentTimeMillis();
            double totalTimeInSecs = (endTime - startTime) / 1000d;
            if (totalTimeInSecs >= 45) {
                double bps = (beats / totalTimeInSecs);
                int dpm = (int) (bps * 60d);
//                if (dpm < 30 || dpm > 180) {
//                    startTime = System.currentTimeMillis();
//                    beats = 0;
//                    processing.set(false);
//                    return;
//                }

                // Log.d(TAG,
                // "totalTimeInSecs="+totalTimeInSecs+" beats="+beats);

                if (beatsIndex == beatsArraySize) beatsIndex = 0;
                beatsArray[beatsIndex] = dpm;
                beatsIndex++;

                int beatsArrayAvg = 0;
                int beatsArrayCnt = 0;
                for (int i = 0; i < beatsArray.length; i++) {
                    if (beatsArray[i] > 0) {
                        beatsArrayAvg += beatsArray[i];
                        beatsArrayCnt++;
                    }
                }
                int beatsAvg = (beatsArrayAvg / beatsArrayCnt);
                ts.setText(String.valueOf(beatsAvg));
                heartRate =beatsAvg;
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(c);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LAST_MEASURE", String.valueOf(beatsAvg));
                editor.commit();

                startTime = System.currentTimeMillis();
                mPreviewRunning = false;
                beats = 0;
            }
            processing.set(false);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setContentView(R.layout.activity_main);
        ts = (TextView) findViewById(R.id.number1);
        SurfaceView = (SurfaceView) findViewById(R.id.bgc);
        SurfaceHolder = SurfaceView.getHolder();
        SurfaceHolder.addCallback(this);
        SurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        tip = (TextView) findViewById(R.id.tipText);
        c = getApplicationContext();

        tip.setText("Please measure Heart and Respiratory Rate First, then click the upload button");
        Button upload = (Button) findViewById(R.id.uploadSigns);

        final TextView respiratoryRateView = findViewById(R.id.respiratory_rate);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
//        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
//        View fg_one = factory.inflate(R.layout.fragment_first, null);
        final Button button = (Button) findViewById(R.id.heartRate);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCamera.startPreview();
                mPreviewRunning = true;
                startTime = System.currentTimeMillis();

                final Handler mHandler = new Handler();
                Runnable r = new Runnable() {

                    @Override
                    public void run() {
                        //do something
                        mPreviewRunning = false;
                        mCamera.stopPreview();
                    }
                };

                mHandler.postDelayed(r, 46 * 1000);
            }
        });


        Button measureRespiratoryRate = findViewById(R.id.measure_respiratory_rate);

        measureRespiratoryRate.setOnClickListener(new Button.OnClickListener() {
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

                    respiratoryRateView.setText(" Collecting accelerometer data for "
                            + (RECORDING_TIME / 1000) + "s...");

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
                                    "success!!!!!!",
                                    Toast.LENGTH_LONG)
                                    .show();
                            System.out.println("Accelerometer readings size: "
                                    + accelerometerReadings.size());

                            respiratoryRate = new RespiratoryRateMonitor()
                                    .calculateRespiratoryRate(accelerometerReadings);
                            respiratoryRateView.setText(String.format("%.1f", respiratoryRate));
                        }
                    }, RECORDING_TIME);
                } else {

                    Toast.makeText(getApplicationContext(),
                            "No Sensor!",
                            Toast.LENGTH_LONG)
                            .show();
                }
            }
        });



        upload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dataBaseSort dataBaseSort = new dataBaseSort();
                dataBaseSort.setUsername(username);
                dataBaseSort.setDate(date);

                dataBaseSort.setHeartRate(heartRate);
                dataBaseSort.setRespiratoryRate(respiratoryRate);

                DatabaseOperate db = new DatabaseOperate(MainActivity.this);
                db.insert(dataBaseSort, "signs");

                Toast.makeText(getApplicationContext(),
                        "success",
                        Toast.LENGTH_SHORT)
                        .show();
                isFinished = true;
                tip.setText("upload the heart and respiratory data success! clike the Symptom button! ");
            }
        });

        Button nextStep = findViewById(R.id.next_page);
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( isFinished == false) {
                    tip.setText("Please upload the heart and respiratory data first! ");
                    return;
                }
                Intent intent = new Intent(MainActivity.this ,MainActivity2.class);
                startActivity(intent);
                MainActivity.this.finish();
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