package com.max_baker.simpleruntracker;
/**
 * Written By: Max Baker
 * Last Modified; 12/7/17
 * Updates running metrics, pressing finish will save the run to a database
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    final int LOCATION_REQUEST_CODE = 1;
    final int ADD_RUN_CODE = 808;
    final static int SMOOTHING_RATE=3;
    int smoothingIndex=0;
    float aggrSpeed=0;
    ArrayList<Double> latitudes = new ArrayList<Double>();
    ArrayList<Double> longitudes = new ArrayList<Double>();
    ArrayList<Double> latitudesStore = new ArrayList<Double>();
    ArrayList<Double> longitudesStore = new ArrayList<Double>();
    final String TAG= "Main Activity";
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
                startButton.setText(R.string.stop);
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
            startButton.setText(R.string.stop);
            running=true;
        }else{
            latitudesStore = latitudes;
            longitudesStore= longitudes;
            startButton.setText(R.string.start);
            timer.cancel();
            timer.purge();
            running=false;
        }
    }


    public void finishRun(View view){
        running=false;
        Intent intent = new Intent(this,RecordsActivity.class);
        intent.putExtra("minutes", minutes);
        intent.putExtra("seconds", seconds);
        float milesRun = (float)(totalDistance/METERS_TO_MILES);
        float milesRunRounded= round(milesRun,2);
        intent.putExtra("distance", milesRunRounded);
        intent.putExtra("request",ADD_RUN_CODE);
        latitudesStore = latitudes;
        longitudesStore=longitudes;
        latitudes.clear();
        longitudes.clear();
        startActivityForResult(intent,ADD_RUN_CODE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch(menuId){
            case R.id.goToRecords:
                Intent intent = new Intent(MainActivity.this, RecordsActivity.class);
                startActivity(intent);
                return true;
            case R.id.goToMap:
                Log.d(TAG, "goToMap case");

                //TODO:Carter fix this
                Intent intent2 = new Intent(MainActivity.this, MapsActivity.class);
                if(running) {
                    Log.d(TAG, "Running case");

                    intent2.putExtra("latitudes",  latitudes);
                    intent2.putExtra("longitudes", longitudes);
                }else{
                    Log.d(TAG, "Not running case");

                    intent2.putExtra("latitudes", latitudesStore);
                    intent2.putExtra("longitudes", longitudesStore);
                }
                Log.d(TAG, "Start Activity Intent 2");

                startActivity(intent2);
                return true;
            default:
                Log.d(TAG, "Default case");

                return super.onOptionsItemSelected(item);
        }

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == ADD_RUN_CODE && resultCode == Activity.RESULT_OK){
            Button startButton = (Button) findViewById(R.id.startButton);
            TextView totalTime = (TextView) findViewById(R.id.timeTotalDisplay);
            TextView distanceDisplay = (TextView) findViewById(R.id.distanceTotalDisplay);
            running=false;
            totalDistance = 0;
            seconds = 0;
            minutes = 0;
            startButton.setText(R.string.start);
            timer.cancel();
            timer.purge();
            totalTime.setText(formatSeconds(minutes) + ":" + formatSeconds(seconds));
            float milesRun = (float)(totalDistance/METERS_TO_MILES);
            String distanceText=round(milesRun,2)  + " miles";
            distanceDisplay.setText(distanceText);
            Toast.makeText(this,"Run Archived",Toast.LENGTH_SHORT).show();
        }
    }

    public void setupSpeedFinder(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            return;
        }
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        LocationRequest locationRequest = new LocationRequest();
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
                displaySpeed.setText(R.string.noSpeed);
                displayTime.setText(R.string.noSpeedTime);
            } else{
                speed= (float) ((location.getSpeed()*SECONDS_TO_HOURS)/METERS_TO_MILES);
                if(minutes<=20) {
                    if(smoothingIndex>=SMOOTHING_RATE) {
                        smoothingIndex=0;
                        float avgSpeed = aggrSpeed/SMOOTHING_RATE;
                        double timePerMile = 60/avgSpeed;
                        int minutes = (int) timePerMile;
                        int seconds = (int) (60*(timePerMile%1));
                        String speedText= round(speed, 2) + " mph";
                        displaySpeed.setText(speedText);
                        displayTime.setText(formatSeconds(minutes) + ":" + formatSeconds(seconds) + " per mile");
                        aggrSpeed=0;
                    }else{
                        smoothingIndex++;
                        aggrSpeed+=speed;
                    }
                    if(running){
                        float distanceTraveled = location.distanceTo(lastLocation);
                        totalDistance+=distanceTraveled;
                        latitudes.add(location.getLatitude());
                        longitudes.add(location.getLongitude());
                    }
                }else{
                    displaySpeed.setText(R.string.noSpeed);
                    displayTime.setText(R.string.noSpeedTime);
                }
                lastLocation=location;
                TextView distanceDisplay = (TextView) findViewById(R.id.distanceTotalDisplay);
                float milesRun = (float)(totalDistance/METERS_TO_MILES);
                String distanceText = round(milesRun,2)  + " miles";
                distanceDisplay.setText(distanceText);
            }

        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }

}


