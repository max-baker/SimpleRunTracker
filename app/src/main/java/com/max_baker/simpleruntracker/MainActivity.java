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

public class MainActivity extends AppCompatActivity implements LocationListener {
    final int LOCATION_REQUEST_CODE = 1;
    final String TAG= "Main Activity";

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
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        this.onLocationChanged(null);
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
    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Do i go first?");
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
            double timePerMile = 60/speed;
            int minutes = (int) timePerMile;
            int seconds = (int) (60*(timePerMile%1));
            if(minutes<=20) {
                displaySpeed.setText(speed + " mph");
                displayTime.setText(minutes + ":" + formatSeconds(seconds) + " per mile");
            }else{
                displaySpeed.setText("00 mph");
                displayTime.setText("00:00");
            }
        }

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}

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

}
