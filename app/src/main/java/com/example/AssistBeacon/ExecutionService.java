package com.example.AssistBeacon;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class ExecutionService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long MIN_TIME = 1000 * 60 * 2;
    private final long MIN_DIST = 20;

    DatabaseReference databaseReference, locRef;

    @Override
    public void onCreate() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Address/");
        locRef = FirebaseDatabase.getInstance().getReference("Location/");

        super.onCreate();
    }

    public ExecutionService() {
//        Toast.makeText(this, "Execution Service Running!", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Execution Service Running!", Toast.LENGTH_LONG).show();
        Log.d("ExecutionService", "Execution Service Running!");
        locationDetector();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("ExecutionService", "Execution Service Destroyed!");
        super.onDestroy();

    }

    private void locationDetector() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {

                    Toast.makeText(getApplicationContext(), "LocationChanged!", Toast.LENGTH_LONG).show();
                    Log.d("ExecutionService", "LocationChanged!");

                    String phoneNumber = "6546768431";
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();
                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String address = null;

                    address = getAddressFromLocation(myLat, myLong, ExecutionService.this);

                    String message = "Latitude = " + myLatitude + "   Longitude = " + myLongitude;


//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phoneNumber,null,message,null,null);

                    String id = databaseReference.push().getKey();

                    locRef.child(id).setValue(message);

                    if (address != null) {
                        message = address;
                    }

                    databaseReference.child(id).setValue(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("ExecutionService", "onStatusChanged triggered!");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("ExecutionService", "onProviderEnabled triggered!");

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, MIN_DIST, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, MIN_DIST, this);

            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("ExecutionService", "onProviderDisabled triggered!");
            }
        };

//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("ExecutionService", "After getting location service");

        try {
            Log.d("ExecutionService", "Requesting location updates");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, MIN_DIST, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d("ExecutionService", "Security Exception Triggered!");
        }


        if(locationManager != null)
        {

            Toast.makeText(getApplicationContext(), "Inside last location condition", Toast.LENGTH_LONG).show();
            Log.d("ExecutionService", "Inside last location condition");

            Location location;

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            }
            else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            else
            {
                return;
            }


            String phoneNumber = "6546768431";
            double myLat = location.getLatitude();
            double myLong = location.getLongitude();
            String myLatitude = String.valueOf(location.getLatitude());
            String myLongitude = String.valueOf(location.getLongitude());

            String address = null;

            address = getAddressFromLocation(myLat, myLong, ExecutionService.this);

            String message = "Latitude = " + myLatitude + "   Longitude = " + myLongitude;


//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phoneNumber,null,message,null,null);

            String id = databaseReference.push().getKey();

            locRef.child(id).setValue(message);

            if (address != null) {
                message = address;
            }

            databaseReference.child(id).setValue(message);
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
//                        sb.append(address);
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
//                            sb.append(address.getAddressLine(i)).append("\n");
                            sb.append(address.getAddressLine(i));
                        }
//                        sb.append(address.getLocality()).append("\n");
//                        sb.append(address.getPostalCode()).append("\n");
//                        sb.append(address.getCountryName());
                        result = sb.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable connect to Geocoder", e);
                }

                return result;
            }

    }

