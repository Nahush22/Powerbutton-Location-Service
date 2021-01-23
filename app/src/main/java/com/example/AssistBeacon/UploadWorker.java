package com.example.AssistBeacon;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

//  See this site for WorkerClass implementation - https://github.com/android/location-samples/issues/220


public class UploadWorker extends Worker {

    private LocationManager locationManager;
    private LocationListener locationListener;

    private final long MIN_TIME = 1000 * 60 * 2;
    private final long MIN_DIST = 20;

    DatabaseReference databaseReference, locRef;

    Context context;

    public UploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        uploadLocation();
        return Result.success();
    }

    private void uploadLocation() {
        // do your repeating task here

        databaseReference = FirebaseDatabase.getInstance().getReference("Address/");
        locRef = FirebaseDatabase.getInstance().getReference("Location/");

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {

//                    Toast.makeText(getApplicationContext(), "LocationChanged!", Toast.LENGTH_LONG).show();
                    Log.d("UploadWorker", "LocationChanged!");

                    String phoneNumber = "6546768431";
                    double myLat = location.getLatitude();
                    double myLong = location.getLongitude();
                    String myLatitude = String.valueOf(location.getLatitude());
                    String myLongitude = String.valueOf(location.getLongitude());

                    String address = null;

                    address = getAddressFromLocation(myLat, myLong, getApplicationContext());

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
                Log.d("UploadWorker", "onStatusChanged triggered!");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("UploadWorker", "onProviderEnabled triggered!");

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
                Log.d("UploadWorker", "onProviderDisabled triggered!");
            }
        };

//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Log.d("UploadWorker", "After getting location service");

        try {
            Log.d("UploadWorker", "Requesting location updates");

            // To fix requestLocationUpdates gives error "Can't create Handler inside thread that has not called Looper.prepare() error
            // https://stackoverflow.com/questions/9033337/requestlocationupdates-gives-error-cant-create-handler-inside-thread-that-has

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, MIN_DIST, locationListener, Looper.getMainLooper());
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, MIN_DIST, locationListener, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
            Log.d("UploadWorker", "Security Exception Triggered!");
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
            Log.e("UploadWorker", "Unable connect to Geocoder", e);
        }

        return result;
    }


}
