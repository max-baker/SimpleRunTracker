package com.max_baker.simpleruntracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.RunnableFuture;


public class MainActivity extends AppCompatActivity {
    final int LOCATION_REQUEST_CODE = 1;
    //timer task
    final String TAG= "Main Activity";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    int seconds;
    int minutes;
    float speed;
    static double METERS_TO_MILES=1609.344;
    static int SECONDS_TO_HOURS=3600;
    Location lastLocation;
    float totalDistance;
    Timer timer;
    boolean running=false;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("seconds", seconds);
        outState.putInt("minutes",minutes);
        outState.putFloat("distance",totalDistance);
        outState.putFloat("speed",speed);
        outState.putBoolean("running",running);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "I should at least get here");
        setContentView(R.layout.activity_main);
        totalDistance=0;
        super.onCreate(savedInstanceState);
        setupSpeedFinder();
        if(savedInstanceState != null){
            seconds = savedInstanceState.getInt("seconds",0);
            minutes = savedInstanceState.getInt("minutes",0);
            totalDistance = savedInstanceState.getFloat("distance",0);
            speed = savedInstanceState.getFloat("speed",0);
            running=savedInstanceState.getBoolean("running");
            if(running){
                Button startButton = (Button) findViewById(R.id.startButton);
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView totalTime = (TextView) findViewById(R.id.timeTotalDisplay);
                                seconds += 1;
                                if (seconds == 60) {
                                    seconds = 0;
                                    minutes++;
                                }
                                totalTime.setText(formatSeconds(minutes) + ":" + formatSeconds(seconds));
                            }
                        });
                    }
                }, 1000, 1000);
                startButton.setText("Stop");
            }
        }else{
            seconds=0;
            minutes=0;
            TextView totalTime = (TextView) findViewById(R.id.timeTotalDisplay);
            totalTime.setText(formatSeconds(minutes)+":"+formatSeconds(seconds));
        }

    }

    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    public void startClick(View view){
        Button startButton = (Button) findViewById(R.id.startButton);
        if(!running) {
            totalDistance = 0;
            seconds = -1;
            minutes = 0;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView totalTime = (TextView) findViewById(R.id.timeTotalDisplay);
                            seconds += 1;
                            if (seconds == 60) {
                                seconds = 0;
                                minutes++;
                            }
                            totalTime.setText(formatSeconds(minutes) + ":" + formatSeconds(seconds));
                        }
                    });
                }
            }, 1000, 1000);
            startButton.setText("Stop");
            running=true;
        }else{
            startButton.setText("Start");
            timer.cancel();
            timer.purge();
            running=false;
        }
    }

    public void setupSpeedFinder(){
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "pre permission request");
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            Log.d(TAG, "post permission request");
            return;
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,new MyLocationListener(),null);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "permission request result");
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LOCATION_REQUEST_CODE ){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                setupSpeedFinder();
            }
        }
    }

    private String formatSeconds(int secondsParam)
    {
        if(secondsParam>=59){
            return "59";
        }else if(secondsParam<10){
            return "0"+String.valueOf(secondsParam);
        }else{
            return String.valueOf(secondsParam);
        }
    }

    private class MyLocationListener extends LocationCallback{
        //public MyLocationListener(){}


        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location location = locationResult.getLastLocation();
            if(lastLocation==null) {
                lastLocation = location;
            }
            TextView displaySpeed = (TextView) findViewById(R.id.speedDisplay);
            TextView displayTime = (TextView) findViewById(R.id.timeDisplay);
            if (location==null){
                // if you can't get speed because reasons :)
                Log.d(TAG, "how would this error?");
                displaySpeed.setText("00 mph");
                displayTime.setText("00:00");
            } else{
                //int speed=(int) ((location.getSpeed()) is the standard which returns meters per second. In this example i converted it to kilometers per hour
                speed= (float) ((location.getSpeed()*SECONDS_TO_HOURS)/METERS_TO_MILES);
                double timePerMile = 60/speed;
                int minutes = (int) timePerMile;
                int seconds = (int) (60*(timePerMile%1));
                if(minutes<=20) {
                    Log.d(TAG, "updated");
                    displaySpeed.setText(round(speed,2) + " mph");
                    displayTime.setText(minutes + ":" + formatSeconds(seconds) + " per mile");
                    float distanceTraveled = location.distanceTo(lastLocation);
                    totalDistance+=distanceTraveled;
                    lastLocation=location;
                }else{
                    displaySpeed.setText("00 mph");
                    displayTime.setText("00:00");
                }
                TextView distanceDisplay = (TextView) findViewById(R.id.distanceTotalDisplay);
                TextView timeDisplay = (TextView) findViewById(R.id.timeTotalDisplay);
                float milesRun = (float)(totalDistance/METERS_TO_MILES);
                distanceDisplay.setText(round(milesRun,2)  + " miles");
            }

        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }

}


