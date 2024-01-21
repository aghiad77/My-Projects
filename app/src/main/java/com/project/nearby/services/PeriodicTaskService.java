package com.project.nearby.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.project.nearby.models.History;
import com.project.nearby.receiver.PeriodicTaskReceiver;

import java.util.ArrayList;
import java.util.List;

public class PeriodicTaskService extends JobIntentService {

    static final int JOB_ID = 1000;
    private static final String TAG = PeriodicTaskReceiver.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    private LocationCallback locationCallback;

    double latitude;
    double longitude;

    FirebaseDatabase firebaseDatabase;

    List<History> historyList = new ArrayList<>();
    private String uid;


    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }
        FirebaseApp.initializeApp(getBaseContext());
        firebaseDatabase = FirebaseDatabase.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
    }

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, PeriodicTaskService.class, JOB_ID, work);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        toast("service started");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getBaseContext());
//
//        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location != null) {
//                Log.d(TAG, "getLastLocation = latLon " + location.getLatitude() + "," + location.getLongitude());
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
//            } else {
//                Log.d(TAG, "getLastLocation is null");
//            }
//        });

        createLocationRequest();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getBaseContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> {
//            getNearBy();
//            contactUserStatusAlert();

        });

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {

                Log.d(TAG, "task fail");
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Log.d(TAG, "getLocationUpdate is null");
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.d(TAG, "getLocationUpdate = lat " + location.getLatitude() + "," + location.getLongitude());
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
                firebaseDatabase.getReference()
                        .child("users")
                        .child(uid)
                        .child("latlon")
                        .setValue(latitude + "," + longitude);
            }
        };

        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
            @Override
            public boolean isCancellationRequested() {
                return false;
            }

            @NonNull
            @Override
            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                return null;
            }
        }).addOnSuccessListener(location -> {
            Log.d(TAG, "getCurrentLocation= "+location.getLatitude()+","+location.getLongitude());
            firebaseDatabase.getReference()
                    .child("users")
                    .child(uid)
                    .child("latlon")
                    .setValue(location.getLatitude() + "," + location.getLongitude());
        });
//        fusedLocationClient.requestLocationUpdates(locationRequest,
//                locationCallback,
//                Looper.getMainLooper());

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(40000);
        locationRequest.setWaitForAccurateLocation(true);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressWarnings("deprecation")
    final Handler mHandler = new Handler();

    // Helper for showing tests
    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(PeriodicTaskService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}
