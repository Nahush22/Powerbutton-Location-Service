package com.example.AssistBeacon;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class LocationForeground extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long MIN_TIME = 1000 * 60 * 2;
    private final long MIN_DIST = 20;

    DatabaseReference databaseReference, locRef;

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public LocationForeground() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.service_notif);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PowerButton App")
                .setContentText(getResources().getString(R.string.LocationString))
                .setSmallIcon(R.mipmap.service_notif)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        startLocationService();

        return START_NOT_STICKY;

    }

    private void startLocationService() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Address/");
        locRef = FirebaseDatabase.getInstance().getReference("Location/");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {

                    Toast.makeText(getApplicationContext(), "LocationChanged!", Toast.LENGTH_LONG).show();
                    Log.d("LocationForegroundService", "LocationChanged!");

                    String phoneNumber = "6546768431";
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();
                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String address = null;

                    address = getAddressFromLocation(myLat, myLong, LocationForeground.this);

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
                Log.d("LocationForegroundService", "onStatusChanged triggered!");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("LocationForegroundService", "onProviderEnabled triggered!");

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
                Log.d("LocationForegroundService", "onProviderDisabled triggered!");
            }
        };

//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("LocationForegroundService", "After getting location service");

        try {
            Log.d("LocationForegroundService", "Requesting location updates");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, MIN_DIST, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, MIN_DIST, locationListener);
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d("LocationForegroundService", "Security Exception Triggered!");
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


    @Override
    public void onDestroy() {
        super.onDestroy();

        if(locationListener != null)
            locationManager.removeUpdates(locationListener);

        Toast.makeText(this, "Stopping location foreground service", Toast.LENGTH_SHORT).show();


    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

    }



}