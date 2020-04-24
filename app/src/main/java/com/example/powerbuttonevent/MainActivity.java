package com.example.powerbuttonevent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private Button startServiceBtn, stopServiceBtn, actPerm;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long MIN_TIME = 10000;
    private final long MIN_DIST = 5;

    DatabaseReference databaseReference, locRef;

    int locationRequestCount = 0;

    int userCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference("Address/"+String.valueOf(userCount));

        locRef = FirebaseDatabase.getInstance().getReference("Location/"+String.valueOf(userCount));

        startServiceBtn = findViewById(R.id.startServ);
        stopServiceBtn = findViewById(R.id.stopServ);
        actPerm = findViewById(R.id.actLoc);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService();
            }
        });

        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
            }
        });

        actPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationDetector();
            }
        });
    }

    private void locationDetector() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {

                    databaseReference = FirebaseDatabase.getInstance().getReference("Address/"+String.valueOf(userCount));

                    locRef = FirebaseDatabase.getInstance().getReference("Location/"+String.valueOf(userCount));

                    userCount++;

                    String phoneNumber = "6546768431";
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();
                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String address = null;

                    address = getAddressFromLocation(myLat, myLong, getApplicationContext());

                    String message = "Latitude = " + myLatitude +"   " + "Longitude = " + myLongitude;

//                    if(address!=null)
//                    {
//                        message = address;
//
//                        Log.d(TAG, "Address stored in message string");
//
//                    }

//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phoneNumber,null,message,null,null);

                    String id = databaseReference.push().getKey();

                    databaseReference.child(id).setValue(message);

                    if(address!=null)
                    {
                        locRef.child(id).setValue(address);
                    }
                    else
                    {
                        locRef.child(id).setValue("Address not found");
                    }

                    if(locationRequestCount == 0)
                    {
                        locationManager.removeUpdates(this);
                        locationManager = null;

                        Log.d(TAG, "LocationManager deactivated");
                    }

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, locationListener);

        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

    }

    public static String getAddressFromLocation(final double latitude, final double longitude, final Context context) {

        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String result = null;
        try {
            List<Address> addressList = geocoder.getFromLocation( latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);
                StringBuilder sb = new StringBuilder();

                Log.d(TAG, "Address obtained from Geocoder");
//                        sb.append(address);

                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                            sb.append(address.getAddressLine(i)).append("\n");
                    sb.append(address.getAddressLine(i));

                    Log.d(TAG, "Address appended to Stringbuilder");

                }
//                        sb.append(address.getLocality()).append("\n");
//                        sb.append(address.getPostalCode()).append("\n");
//                        sb.append(address.getCountryName());

                Log.d(TAG, "Stringbuilder stored to result");

                result = sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Unable connect to Geocoder", e);
        }

        Log.d(TAG, "Result is returned");

        return result;
    }

    private void startService() {

        Intent serviceIntent = new Intent(this, MyService.class);
        serviceIntent.putExtra("inputExtra", "Foreground Service Example in Android");

        ContextCompat.startForegroundService(this, serviceIntent);

    }

    private void stopService() {

        Intent serviceIntent = new Intent(this, MyService.class);
        stopService(serviceIntent);

    }


}
