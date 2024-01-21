package com.project.nearby.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.project.nearby.activities.MainActivity;
import com.project.nearby.models.Ble;
import com.project.nearby.models.History;
import com.project.nearby.models.Message;
import com.project.nearby.models.User;
import com.project.nearby.utils.Utils;
import com.project.nearby.viewModels.FirebaseViewModel;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";
    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    private static final String CHANNEL_ID = "location_channel";
    public static Format formatd = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss" , Locale.ENGLISH);
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss", Locale.ENGLISH);
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    private LocationCallback locationCallback;
    FirebaseViewModel firebaseViewModel;
    List<History> historyList = new ArrayList<>();
    double latitude;
    double longitude;
    FirebaseDatabase firebaseDatabase;
    private String uid;
    private List<Ble> bleList = new ArrayList<>();

    BluetoothAdapter bluetoothAdapter;

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase = FirebaseDatabase.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            if (intent.getAction() != null){
                if (intent.getAction().equals(ACTION_STOP_FOREGROUND_SERVICE)){
                    stopForegroundService();
                }else {
                    getLocation();
                    getUsers();
                    getUsersUpdatedStatusMutableLiveData();
                    getMessages();
                    onBluetooth();
                    proceedDiscovery();
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(myReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                    .setContentTitle("background service")
                    .setContentText("location").build();

            startForeground(1, notification);
        }

    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

        SettingsClient client = LocationServices.getSettingsClient(this);
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

//        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
//            @Override
//            public boolean isCancellationRequested() {
//                return false;
//            }
//
//            @NonNull
//            @Override
//            public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
//                return null;
//            }
//        }).addOnSuccessListener(location -> {
//            Log.d(TAG, "getCurrentLocation= "+location.getLatitude()+","+location.getLongitude());
//            firebaseDatabase.getReference()
//                    .child("users")
//                    .child(uid)
//                    .child("latlon")
//                    .setValue(location.getLatitude() + "," + location.getLongitude());
//        });
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());

//        try {
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60000);
        locationRequest.setFastestInterval(20000);
        locationRequest.setWaitForAccurateLocation(true);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void getUsers() {
        firebaseDatabase.getReference().child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChildren()){

                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.id.equals(uid)){
                        double lat = Double.parseDouble(user.latlon.split(",")[0]);
                        double lon = Double.parseDouble(user.latlon.split(",")[1]);
                        double distance = Utils.getDistance(latitude, longitude, lat, lon);
                        Log.d(TAG, "getNearBy distance = " + distance);
                        if (distance < 1.5) {
                            boolean isExist = false;
                            for (History history : historyList) {
                                if (history.getUserId().equals(user.id)){
                                    isExist = true;
                                }
                            }
                            if (!isExist){
                                new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("Please maintain safe distance");
                            }
                        }
                        if (distance < 5) {
                            boolean isExist = false;
                            for (History history1 : historyList) {
                                if (history1.getUserId().equals(user.id)) {
                                    try {
                                        history1.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(history1.getTimeStamp()).getTime()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    isExist = true;
                                }
                            }
                            if (!isExist) {
                                double bledistance = getRssiDistance();
                                if (bledistance < 1.5) {
                                    new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("Please maintain safe distance");
                                }
                                History Huser=new History(user.id, user.name, 0, distance, bledistance,formatd.format(new Date()));
                                historyList.add(Huser);
                                firebaseDatabase.getReference().child("history").child(uid).push().setValue(Huser);
                                sendBroadcastToHome(user.id,"user");
                            }
                        } else {
                            for (History data : historyList) {
                                if (data.getUserId().equals(user.id)) {
                                    try {
                                        data.setTimeSpent(Utils.timeDifference(new Date().getTime(),sdf.parse(data.getTimeStamp()).getTime()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    firebaseDatabase.getReference().child("history").child(uid).push().setValue(data);
                                    historyList.remove(data);
                                }
                            }
                        }
                    }
                    Log.d(TAG,user.name);
                    Log.d(TAG, String.valueOf(user.status));
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void refreshHistory(String id, double distance, long timeSpent) {
        firebaseDatabase.getReference()
                .child("history")
                .child(id)
                .child("distance")
                .setValue(distance);

        firebaseDatabase.getReference()
                .child("history")
                .child(id)
                .child("timeSpent")
                .setValue(timeSpent);

        /*firebaseDatabase.getReference()
                .child("history")
                .child(id)
                .child("ble_distance")
                .setValue(ble);*/
    }

    private void storeHistory(History Huser) {
        firebaseViewModel.addHistory(Huser);
    }

    private void stopForegroundService() {
        Log.d(TAG, "Stop foreground service.");
        stopLocationUpdates();

        // Stop foreground service and remove the notification.
        stopForeground(true);

        // Stop the foreground service.
        stopSelf();
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    public void checkHistory(String userId){

        firebaseDatabase.getReference().child("history").child(uid).orderByChild("userId").equalTo(userId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot :
                        snapshot.getChildren()) {
                    History history = dataSnapshot.getValue(History.class);
                    assert history != null;
                    long difference = 0;
                    try {
                        difference = new Date().getTime() - sdf.parse(history.getTimeStamp()).getTime();
                        if (Utils.inTime(difference)){
                            if (history.getDistance() <= 1.5 && history.getTimeSpent() >= 5){
                                new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                            }else if (history.getDistance() <= 3.5 && history.getTimeSpent() <= 10){
                                new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                            }else if (history.getDistance() <= 5.5 && history.getTimeSpent() <= 15){
                                new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void getUsersUpdatedStatusMutableLiveData() {
        firebaseDatabase.getReference().child("status").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChildren()){

                    Boolean status = (Boolean) snapshot.child("status").getValue();

                    Log.d(TAG,"previousChildName " +previousChildName+ "value="+status);
                    if (status && !snapshot.getKey().equals(uid)){
                        checkHistory(snapshot.getKey());
                        sendBroadcastToHome(snapshot.getKey(),"status");
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void sendBroadcastToHome(String id,String status){
        Intent intent = new Intent("ACTION_GET_NEIGHBOURS");
        intent.putExtra("userId", id);
        intent.putExtra("status", status);
        LocalBroadcastManager.getInstance(LocationService.this).sendBroadcast(intent);
    }

    public void getMessages(){
        firebaseDatabase.getReference().child("messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                if (snapshot.hasChildren()){
                    try {
                        Message message = snapshot.getValue(Message.class);
                        String user_id = message.getUserId();
                        String body = message.getBody();
                        if (user_id.contains(uid)) {
                            new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification(body);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getApplication(),e.toString(),Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                try {
                    int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                    Toast.makeText(getApplication(),String.valueOf(RSSI),Toast.LENGTH_LONG).show();
                    if(!bleList.isEmpty())
                        bleList.clear();
                    bleList.add(new Ble(RSSI,formatd.format(new Date()),"BluetoothReceiver"));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    private void onBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
            Log.i("Log", "Bluetooth is Enabled");
        }
    }

    protected void proceedDiscovery() {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        registerReceiver(myReceiver, filter);
        bluetoothAdapter.startDiscovery();
    }

    private double getBleDistance(int rssi) {
        int Txpower = (int) ((Math.round(rssi / Math.exp(Math.log(Math.abs((1-0.111)/0.89976))/7.7095) * 100)) / 100.0);
        double distance = Math.pow(10d, ((double) Txpower - rssi) / (10 * 2));
        return (int) (Math.round(distance * 100)) / 100.0;
    }

    private double getBleDistanceB( int rssi){

        if (rssi > -45) {
            return 0;
        }else if ( -45 > rssi && rssi >= -51){
            return 1;
        }else if ( -47 > rssi && rssi >= -51){
            return 2;
        }else if ( -51 > rssi && rssi >= -54){
            return 3;
        }else if ( -54 > rssi && rssi >= -58){
            return 4;
        }else if ( -58 > rssi && rssi >= -61){
            return 5;
        }else if ( -61 > rssi && rssi >= -68){
            return 6;
        }else if ( -68 > rssi && rssi >= -71){
            return 7;
        }else if ( -71 > rssi && rssi >= -76){
            return 8;
        }else if ( -76 > rssi && rssi >= -80){
            return 9;
        }else if ( -80 > rssi){
            return 10;
        }
        return 0;
    }

    private double getRssiDistance() {
        if(!bleList.isEmpty()){
            for (Ble ble : bleList){
                try {
                    if( (int) Utils.timeDifference(new Date().getTime(),sdf.parse(ble.getTimestamp()).getTime()) < 15){
                        double distance=0;
                        if( getBleDistance(ble.getRssi()) < getBleDistanceB(ble.getRssi()))
                            distance = getBleDistance(ble.getRssi());
                        else
                            distance = getBleDistanceB(ble.getRssi());
                        if( getBleDistanceB(ble.getRssi()) == 0) {
                            distance = getBleDistance(ble.getRssi());
                        }
                        if (distance <= 1.5 && (int) Utils.timeDifference(new Date().getTime(),sdf.parse(ble.getTimestamp()).getTime()) >= 5){
                            new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                        }else if (distance <= 3.5 && (int) Utils.timeDifference(new Date().getTime(),sdf.parse(ble.getTimestamp()).getTime()) <= 10){
                            new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                        }else if (distance <= 5.5 && (int) Utils.timeDifference(new Date().getTime(),sdf.parse(ble.getTimestamp()).getTime()) <= 15){
                            new com.project.nearby.utils.Notification(getApplicationContext()).buildNotification("There is a covid positive in your contact list");
                        }
                        return distance;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}
