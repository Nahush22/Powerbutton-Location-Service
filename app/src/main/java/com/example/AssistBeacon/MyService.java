package com.example.AssistBeacon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.annotations.NotNull;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import github.com.vikramezhil.dks.speech.Dks;
import github.com.vikramezhil.dks.speech.DksListener;

public class MyService extends Service {

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    BroadcastReceiver mReceiver;

    private Dks dks;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        String input = intent.getStringExtra("The service to monitor power button click is running...");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.service_notif);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PowerButton App")
                .setContentText(getResources().getString(R.string.NotificationString))
                .setSmallIcon(R.mipmap.service_notif)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        //getSupportFragmentManager() -> ((FragmentActivity) getApplicationContext()).getSupportFragmentManager(); --- https://stackoverflow.com/questions/24762394/get-current-fragment-from-service

        dks = new Dks(getApplication(), new FragmentManager() {
            @NonNull
            @Override
            public FragmentTransaction beginTransaction() {
                return null;
            }

            @Override
            public boolean executePendingTransactions() {
                return false;
            }

            @Nullable
            @Override
            public Fragment findFragmentById(int id) {
                return null;
            }

            @Nullable
            @Override
            public Fragment findFragmentByTag(@Nullable String tag) {
                return null;
            }

            @Override
            public void popBackStack() {

            }

            @Override
            public boolean popBackStackImmediate() {
                return false;
            }

            @Override
            public void popBackStack(@Nullable String name, int flags) {

            }

            @Override
            public boolean popBackStackImmediate(@Nullable String name, int flags) {
                return false;
            }

            @Override
            public void popBackStack(int id, int flags) {

            }

            @Override
            public boolean popBackStackImmediate(int id, int flags) {
                return false;
            }

            @Override
            public int getBackStackEntryCount() {
                return 0;
            }

            @NonNull
            @Override
            public BackStackEntry getBackStackEntryAt(int index) {
                return null;
            }

            @Override
            public void addOnBackStackChangedListener(@NonNull OnBackStackChangedListener listener) {

            }

            @Override
            public void removeOnBackStackChangedListener(@NonNull OnBackStackChangedListener listener) {

            }

            @Override
            public void putFragment(@NonNull Bundle bundle, @NonNull String key, @NonNull Fragment fragment) {

            }

            @Nullable
            @Override
            public Fragment getFragment(@NonNull Bundle bundle, @NonNull String key) {
                return null;
            }

            @NonNull
            @Override
            public List<Fragment> getFragments() {
                return null;
            }

            @Nullable
            @Override
            public Fragment.SavedState saveFragmentInstanceState(@NonNull Fragment f) {
                return null;
            }

            @Override
            public boolean isDestroyed() {
                return false;
            }

            @Override
            public void registerFragmentLifecycleCallbacks(@NonNull FragmentLifecycleCallbacks cb, boolean recursive) {

            }

            @Override
            public void unregisterFragmentLifecycleCallbacks(@NonNull FragmentLifecycleCallbacks cb) {

            }

            @Nullable
            @Override
            public Fragment getPrimaryNavigationFragment() {
                return null;
            }

            @Override
            public void dump(@NonNull String prefix, @Nullable FileDescriptor fd, @NonNull PrintWriter writer, @Nullable String[] args) {

            }

            @Override
            public boolean isStateSaved() {
                return false;
            }
        }, new DksListener() {
            @Override
            public void onDksLiveSpeechResult(@NotNull String liveSpeechResult) {
                Log.d(getPackageName(), "Speech result - " + liveSpeechResult);
//                Toast.makeText(getApplicationContext(), "Live: " + " " + liveSpeechResult, Toast.LENGTH_SHORT).show();

                Log.d("MyService", "LiveSpeech : " + liveSpeechResult);

//                dks.startSpeechRecognition();
                if(liveSpeechResult.toLowerCase().equals("help"))
                {
                    Log.d("MyService", "Inside LiveSpeech help condition");
//                    startService(new Intent(getApplicationContext(), ExecutionService.class));
                    startLocationForegroundService();
                }
            }

            @Override
            public void onDksFinalSpeechResult(@NotNull String speechResult) {
                Log.d(getPackageName(), "Final Speech result - " + speechResult);
                Toast.makeText(getApplicationContext(), "Final: " + " " + speechResult, Toast.LENGTH_SHORT).show();

                Log.d("MyService", "Speech : " + speechResult);

                if(speechResult.toLowerCase().equals("help"))
                {
                    Log.d("MyService", "Inside Speech help condition");
//                    startService(new Intent(getApplicationContext(), ExecutionService.class));
                    startLocationForegroundService();
                }

//                dks.startSpeechRecognition();
            }

            @Override
            public void onDksLiveSpeechFrequency(float frequency) {
            }

            @Override
            public void onDksLanguagesAvailable(@org.jetbrains.annotations.Nullable String defaultLanguage, @org.jetbrains.annotations.Nullable ArrayList<String> supportedLanguages) {
                Log.d(getPackageName(), "defaultLanguage - " + defaultLanguage);
                Log.d(getPackageName(), "supportedLanguages - " + supportedLanguages);

                if (supportedLanguages != null && supportedLanguages.contains("en-IN")) {
                    // Setting the speech recognition language to english india if found
                    dks.setCurrentSpeechLanguage("en-IN");
                }
            }

            @Override
            public void onDksSpeechError(@NotNull String errMsg) {
                Toast.makeText(getApplication(), errMsg, Toast.LENGTH_SHORT).show();
            }
        });

//        dks.injectProgressView(R.layout.layout_pv_inject);
        dks.setOneStepResultVerify(false);

        dks.startSpeechRecognition();
        

        //do heavy work on a background thread

        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new EvenReceiver();
        registerReceiver(mReceiver, filter);


        //stopSelf();

        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        dks.closeSpeechOperations(); //Closing speech recognition
        Toast.makeText(this, "Stopping power button foreground service", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
//        throw new UnsupportedOperationException("Not yet implemented");
        return null;
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

    private void startLocationForegroundService() {

        Intent serviceIntent = new Intent(this, LocationForeground.class);
        serviceIntent.putExtra("inputExtra", "Location Foreground Service initiation");

        ContextCompat.startForegroundService(this, serviceIntent);

    }


}
