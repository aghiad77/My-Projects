package com.project.nearby.receiver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

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
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.project.nearby.models.History;
import com.project.nearby.services.PeriodicTaskService;
import com.project.nearby.worker.LocationUpdateWorker;

import java.util.ArrayList;
import java.util.List;

public class PeriodicTaskReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 12345;
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
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"broadcast onReceive");

//        FirebaseApp.initializeApp(context);
//        firebaseDatabase = FirebaseDatabase.getInstance();
//        uid = FirebaseAuth.getInstance().getUid();

//        PeriodicTaskService.enqueueWork(context,new Intent(context, PeriodicTaskService.class));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                context.startForegroundService(new Intent(context, PeriodicTaskService.class));
//
//        } else {
//            context.startService(new Intent(context, PeriodicTaskService.class));
//        }

        WorkRequest myWorkRequest = OneTimeWorkRequest.from(LocationUpdateWorker.class);
        WorkManager.getInstance(context).enqueue(myWorkRequest);
    }

    @SuppressLint("MissingPermission")
    private void getLocation(Context context){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
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

        SettingsClient client = LocationServices.getSettingsClient(context);
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

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(40000);
        locationRequest.setWaitForAccurateLocation(true);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
