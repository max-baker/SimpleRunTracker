package com.max_baker.simpleruntracker;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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


public class MainActivity extends AppCompatActivity {
    final int LOCATION_REQUEST_CODE = 1;
    final String TAG= "Main Activity";
    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "I should at least get here");
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);
        setupSpeedFinder();

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
        locationRequest.setInterval(5000);
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,new MyLocationListener(), null);
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
            Log.d(TAG, "Do i go first?");
            Location location = locationResult.getLastLocation();
            TextView displaySpeed = (TextView) findViewById(R.id.speedDisplay);
            TextView displayTime = (TextView) findViewById(R.id.timeDisplay);
            if (location==null){
                // if you can't get speed because reasons :)
                Log.d(TAG, "how would this error?");
                displaySpeed.setText("00 mph");
                displayTime.setText("00:00");
            } else{
                //int speed=(int) ((location.getSpeed()) is the standard which returns meters per second. In this example i converted it to kilometers per hour
                Log.d(TAG, "I should not be here");
                double speed= (location.getSpeed()*3600)/1609.344;
                Log.d(TAG, String.valueOf(speed));
                double timePerMile = 60/speed;
                int minutes = (int) timePerMile;
                int seconds = (int) (60*(timePerMile%1));
                if(minutes<=20) {
                    Log.d(TAG, "updated");
                    displaySpeed.setText(speed + " mph");
                    displayTime.setText(minutes + ":" + formatSeconds(seconds) + " per mile");
                }else{
                    displaySpeed.setText("00 mph");
                    displayTime.setText("00:00");
                }
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
        }
    }

}


