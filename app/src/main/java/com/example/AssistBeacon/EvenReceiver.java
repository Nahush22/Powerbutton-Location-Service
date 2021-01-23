package com.example.AssistBeacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import javax.xml.transform.Result;

public class EvenReceiver extends BroadcastReceiver {

    public static boolean wasScreenOn = true;

    public int count = 0;
    public long start, end, diff;

    Context context;


    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");

        this.context = context;

        Log.e("LOB","onReceive");
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            // do whatever you need to do here
            wasScreenOn = false;

            Log.e("LOB","wasScreenOn"+wasScreenOn);
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            // and do whatever you need to do here
            wasScreenOn = true;

            if(count == 0)
            {
                start = System.currentTimeMillis();
            }
            count+=1;
            if(count==2) {
                end = System.currentTimeMillis();
                diff = end - start;

                count=0;

                if(diff <= 2000) {
                    Toast.makeText(context, "Power button clicked", Toast.LENGTH_LONG).show();
//                    context.startService(new Intent(context, ExecutionService.class));
//                    context.startService(new Intent(context, GoogleService.class));
//                    startLocationForegroundService();
                    createWorker();
                }
            }


//            Toast.makeText(context, "Power button clicked", Toast.LENGTH_LONG).show();

//            Intent i = new Intent();
//            i.setClassName("com.example.AssistBeacon", "com.example.AssistBeacon.SecondActivity");
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(i);

//            context = context.getApplicationContext();
//
//            Intent intent1 = new Intent(context, SecondActivity.class);
////            intent1.setClassName(context.getPackageName(), SecondActivity.class.getName());
////            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent1);

//            context.startActivity(new Intent(context, SecondActivity.class));
        }
//        else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
//            Log.e("LOB","userpresent");
//            Log.e("LOB","wasScreenOn"+wasScreenOn);
//            String url = "http://www.stackoverflow.com";
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.setData(Uri.parse(url));
//            context.startActivity(i);
//        }

    }

    private void startLocationForegroundService() {

        Intent serviceIntent = new Intent(context, LocationForeground.class);
        serviceIntent.putExtra("inputExtra", "Location Foreground Service initiation");

        ContextCompat.startForegroundService(context, serviceIntent);

    }


    private void createWorker(){

        PeriodicWorkRequest periodicWorkRequest =
                new PeriodicWorkRequest.Builder(UploadWorker.class, 15, TimeUnit.MINUTES)
                        .build();

        WorkManager.getInstance(context)
                .enqueue(periodicWorkRequest);

    }



}
