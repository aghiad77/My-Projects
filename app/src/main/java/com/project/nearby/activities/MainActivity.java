package com.project.nearby.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import android.Manifest;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.project.nearby.R;
import com.project.nearby.adapters.HistoryRecyclerViewAdapter;
import com.project.nearby.adapters.NeighbourRecyclerViewAdapter;
import com.project.nearby.fragments.CovidFragment;
import com.project.nearby.fragments.HistoryFragment;
import com.project.nearby.fragments.HomeFragment;
import com.project.nearby.models.Ble;
import com.project.nearby.models.History;
import com.project.nearby.models.User;
import com.project.nearby.receiver.PeriodicTaskReceiver;
import com.project.nearby.services.LocationService;
import com.project.nearby.utils.Notification;
import com.project.nearby.utils.Sharedprefs;
import com.project.nearby.utils.Utils;
import com.project.nearby.viewModels.FirebaseViewModel;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.google.android.gms.nearby.messages.Strategy.BLE_ONLY;
import static com.google.android.gms.nearby.messages.Strategy.DISCOVERY_MODE_BROADCAST;
import static com.google.android.gms.nearby.messages.Strategy.DISTANCE_TYPE_EARSHOT;
import static com.project.nearby.activities.Authentication.REQUIRED_PERMISSIONS;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSION_REQUEST_CODE = 1;
    private FirebaseViewModel firebaseViewModel;
    private List<History> historyList = new ArrayList<>();
    private MessagesClient mMessagesClient;
    private Message mMessage;
    public static Format formatd = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss" , Locale.ENGLISH);
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss", Locale.ENGLISH);
    private FusedLocationProviderClient fusedLocationClient;
    //P2P strategy instance for discovering and advertizing to other devices
    public final com.google.android.gms.nearby.connection.Strategy STRATIGY = com.google.android.gms.nearby.connection.Strategy.P2P_STAR;

    //P2P strategy instance for discovering and advertizing to other devices
    private static final Strategy mMessageStrategy = new Strategy.Builder().setDistanceType(DISTANCE_TYPE_EARSHOT).setDiscoveryMode(DISCOVERY_MODE_BROADCAST).build();
    private MessageListener mMessageListener;

    private LocationRequest locationRequest;
    // location last updated time
    private String mLastUpdateTime;

    // location updates interval - 10sec
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    // fastest updates interval - 5 sec
    // location updates will be received if another app is requesting the locations
    // than your app can handle
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private static final int REQUEST_CHECK_SETTINGS = 100;

    // bunch of location related apis
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;

    // boolean flag to toggle the ui
    private Boolean mRequestingLocationUpdates;
    private double latitude, longitude;
    IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CallNearbyMessagesListener();
        InitialLocation();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            item.setChecked(true);
            switch (item.getItemId()) {
                case R.id.page_1:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, HomeFragment.newInstance()).commit();
                    break;
                case R.id.page_2:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, HistoryFragment.newInstance()).commit();
                    break;
                case R.id.page_3:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, CovidFragment.newInstance()).commit();
                    break;
                case R.id.log_out:
                    new MaterialAlertDialogBuilder(this)
                            .setMessage(getResources().getString(R.string.long_message))
                            .setPositiveButton("Yes", (dialogInterface, i) -> {
                                new Sharedprefs(this).removeVariables();
                                FirebaseAuth.getInstance().signOut();
                                this.finish();
                                startActivity(new Intent(this, Authentication.class));
                                Intent intent = new Intent(this, LocationService.class);
                                intent.setAction(LocationService.ACTION_STOP_FOREGROUND_SERVICE);
                                startService(intent);
                            }).setNegativeButton("No", (dialogInterface, i) -> {
                    })
                            .show();
                    break;
            }
            return false;
        });

        firebaseViewModel = new ViewModelProvider(this).get(FirebaseViewModel.class);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, HomeFragment.newInstance()).commit();
        createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkLocationPermission();
        checkPermissions();

        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(LocationService.ACTION_START_FOREGROUND_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }

        //configuring publishing a device
        PublishOptions publishOptions = new PublishOptions.Builder()
                .setStrategy(mMessageStrategy)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                })
                .build();
        Nearby.getMessagesClient(this).publish(mMessage);

        //configuring subscribing a device
        SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
                .setStrategy(Strategy.BLE_ONLY)
                .build();
        Nearby.getMessagesClient(this).subscribe(mMessageListener, subscribeOptions);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates && checkLocationPermissions()) {
            startLocationUpdates();
        }
        updateLocation();
    }

    @Override
    protected void onPause() {
        if (mRequestingLocationUpdates) {
            // pausing location updates
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    public void onStop() {
        Nearby.getMessagesClient(this).unpublish(mMessage);
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (fusedLocationClient != null) {
                stopLocationUpdates();
            }
        }
        super.onDestroy();
    }

    private void CallNearbyMessagesListener() {
        // listener for discovered devices
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                Log.d(TAG, "Found message: " + new String(message.getContent()));
                //firebaseViewModel.addBle(new Ble(new String(message.getContent()), 0, System.currentTimeMillis() / 1000L, "onFound"));
                firebaseViewModel.getUserMutableLiveData(new String(message.getContent())).observe(MainActivity.this, user -> {
                    showLastKnownLocation();
                    double lat = Double.parseDouble(user.latlon.split(",")[0]);
                    double lon = Double.parseDouble(user.latlon.split(",")[1]);
                    double distance = Utils.getDistance(latitude, longitude, lat, lon);
                    Log.d(TAG, "getNearBy distance = " + distance);
                    if (distance < 1.5) {
                        new Notification(getApplicationContext()).buildNotification("Please maintain safe distance");
                    }
                    if (distance < 5) {
                        boolean isExist = false;
                        for (History history1 : historyList) {
                            if (history1.getUserId().equals(user.id)) {
                                history1.setDistance(distance);
                                try {
                                    history1.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(history1.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                isExist = true;
                            }
                        }
                        if (!isExist) {
                            historyList.add(new History(user.id, user.name, 0, distance, 0, formatd.format(new Date())));
                        }
                    } else {
                        for (History data : historyList) {
                            if (data.getUserId().equals(user.id)) {
                                try {
                                    data.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(data.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                firebaseViewModel.addHistory(data);
                                historyList.remove(data);
                            }
                        }
                    }
                });
            }

            @Override
            public void onBleSignalChanged(final Message message, final BleSignal bleSignal) {
                Log.i(TAG, "Message: " + message + " has new BLE signal information: " + bleSignal);
                Double distance = getBleDistance(bleSignal.getRssi(), bleSignal.getTxPower());
                //firebaseViewModel.addBle(new Ble(new String(message.getContent()), distance, System.currentTimeMillis() / 1000L, "onBleSignalChanged"));
                firebaseViewModel.getUserMutableLiveData(new String(message.getContent())).observe(MainActivity.this, user -> {
                    Log.d(TAG, "getNearBy distance = " + distance);
                    showLastKnownLocation();
                    double lat = Double.parseDouble(user.latlon.split(",")[0]);
                    double lon = Double.parseDouble(user.latlon.split(",")[1]);
                    double distancegps = Utils.getDistance(latitude, longitude, lat, lon);
                    if (distance < 1.5) {
                        new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("Please maintain safe distance");
                    }
                    if (distance < 5) {
                        boolean isExist = false;
                        for (History history1 : historyList) {
                            if (history1.getUserId().equals(user.id)) {
                                history1.setBle_distance(distance);
                                try {
                                    history1.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(history1.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                isExist = true;

                            }
                        }
                        if (!isExist) {
                            History Huser = new History(user.id, user.name, 0, distancegps, distance, formatd.format(new Date()));
                            historyList.add(Huser);
                            storeHistory(Huser);
                        }
                    } else {
                        for (History data : historyList) {
                            if (data.getUserId().equals(user.id)) {
                                try {
                                    data.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(data.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                firebaseViewModel.addHistory(data);
                                historyList.remove(data);
                            }
                        }
                    }
                });
            }

            @Override
            public void onDistanceChanged(final Message message, final Distance distance) {
                Log.i(TAG, "Distance changed, message: " + message + ", new distance: " + distance);
                //firebaseViewModel.addBle(new Ble(new String(message.getContent()), distance.getMeters(), System.currentTimeMillis() / 1000L, "onDistanceChanged"));
                firebaseViewModel.getUserMutableLiveData(new String(message.getContent())).observe(MainActivity.this, user -> {
                    Log.d(TAG, "getNearBy distance = " + distance);
                    showLastKnownLocation();
                    double lat = Double.parseDouble(user.latlon.split(",")[0]);
                    double lon = Double.parseDouble(user.latlon.split(",")[1]);
                    double distancegps = Utils.getDistance(latitude, longitude, lat, lon);
                    if (distance.getMeters() < 1.5) {
                        new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("Please maintain safe distance");
                    }
                    if (distance.getMeters() < 5) {
                        boolean isExist = false;
                        for (History history1 : historyList) {
                            if (history1.getUserId().equals(user.id)) {
                                history1.setBle_distance(distance.getMeters());
                                try {
                                    history1.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(history1.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                isExist = true;

                            }
                        }
                        if (!isExist) {
                            History Huser = new History(user.id, user.name, 0, distancegps, distance.getMeters(), formatd.format(new Date()));
                            historyList.add(Huser);
                            storeHistory(Huser);
                        }
                    } else {
                        for (History data : historyList) {
                            if (data.getUserId().equals(user.id)) {
                                try {
                                    data.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(data.getTimeStamp()).getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                firebaseViewModel.addHistory(data);
                                historyList.remove(data);
                            }
                        }
                    }
                });
            }

            @Override
            public void onLost(Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
                //firebaseViewModel.addBle(new Ble(new String(message.getContent()), 0, System.currentTimeMillis() / 1000L, "onLost"));
            }
        };

        mMessage = new Message(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()).getBytes());
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(20000);
        locationRequest.setFastestInterval(10000);
        locationRequest.setWaitForAccurateLocation(true);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void storeHistory() {
        if (!historyList.isEmpty()) {
            for (History history : historyList) {
                firebaseViewModel.addHistory(history);
            }
        }
    }

    private void storeHistory(History Huser) {
        firebaseViewModel.addHistory(Huser);
    }

    private void InitialLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());

                updateLocation();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void updateLocation() {
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
        }
    }

    private void startLocationUpdates() {
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        Toast.makeText(getApplicationContext(), "Started location updates!", Toast.LENGTH_SHORT).show();

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                        updateLocation();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);

                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }

                        updateLocation();
                    }
                });
    }

    private boolean checkLocationPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void stopLocationUpdates() {
        // Removing location updates
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "Location updates stopped!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showLastKnownLocation() {
        if (mCurrentLocation != null) {
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();
        } else {
            //Toast.makeText(getApplicationContext(), "Last known location is not available!", Toast.LENGTH_SHORT).show();
        }
    }

    void checkPermissions() {
        Dexter.withContext(this)
                .withPermissions(REQUIRED_PERMISSIONS)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (!report.areAllPermissionsGranted()) {
                            Toast.makeText(getApplicationContext(), "Please allow this app to access location", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMessagesClient = Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build());
        } else {
            //requestPermission();
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(this, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    protected void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    //TODO re-request
                }
                break;
            }
        }
    }

    double getBleDistance(int rssi, int txPower) {
        return Math.pow(10d, ((double) txPower - rssi) / (10 * 2));
    }

}