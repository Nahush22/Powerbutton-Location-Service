package com.example.powerbuttonevent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

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

    DatabaseReference databaseReference;

    @Override
    public void onCreate() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Location");

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
        locationDetector();

        return START_NOT_STICKY;
    }

    private void locationDetector() {

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {

                    String phoneNumber = "6546768431";
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();
                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String address = null;

                    address = getAddressFromLocation(myLat, myLong, ExecutionService.this);

                    String message = "Latitude = " + myLatitude + "Longitude = " + myLongitude;

                    if(address!=null)
                    {
                        message = address;
                    }

//                    SmsManager smsManager = SmsManager.getDefault();
//                    smsManager.sendTextMessage(phoneNumber,null,message,null,null);

                    String id = databaseReference.push().getKey();

                    databaseReference.child(id).setValue(message);
                } catch (Exception e) {
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, MIN_DIST, locationListener);
        } catch (SecurityException e) {
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

