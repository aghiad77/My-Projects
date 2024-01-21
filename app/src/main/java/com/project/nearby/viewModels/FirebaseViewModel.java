package com.project.nearby.viewModels;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.project.nearby.models.Ble;
import com.project.nearby.models.History;
import com.project.nearby.models.Message;
import com.project.nearby.models.User;
import com.project.nearby.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FirebaseViewModel extends ViewModel {

    private static final String TAG = FirebaseViewModel.class.getSimpleName();
    FirebaseDatabase firebaseDatabase;
    public static final String userRef = "users";
    public static final String userStatusRef = "status";
    public static final String userHistory = "history";
    public static final String userMessage = "messages";
    public static final String userBle = "ble";
    public static final String userLocationRef = "latlon";
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss", Locale.ENGLISH);
    String uid;
    public MutableLiveData<User> userMutableLiveData = new MutableLiveData<>();
    public MutableLiveData<User> currentUserMutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<User>> usersMutableLiveData = new MutableLiveData<>();
    MutableLiveData<User> userStatusAlertMutableLiveData = new MutableLiveData<>();
    MutableLiveData<Boolean> userExist = new MutableLiveData<>();
    MutableLiveData<Boolean> userStatus = new MutableLiveData<>();
    MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    MutableLiveData<List<History>> userHistoryMutableLiveData = new MutableLiveData<>();
    MutableLiveData<List<Message>> userMessageMutableLiveData = new MutableLiveData<>();

    public FirebaseViewModel(){
        firebaseDatabase = FirebaseDatabase.getInstance();
        uid = FirebaseAuth.getInstance().getUid();
    }

    public void setHeathStatus(boolean status){
        firebaseDatabase.getReference().child(userStatusRef).child(uid).child("status").setValue(status);
        firebaseDatabase.getReference().child(userRef).child(uid).child("status").setValue(status);
    }

    public void addHistory(History history){
        firebaseDatabase.getReference().child(userHistory).child(uid).push().setValue(history);
    }

    public void addMessage(Message message){
        firebaseDatabase.getReference().child(userMessage).push().setValue(message);
    }

    public void addBle(Ble ble){
        firebaseDatabase.getReference().child(userBle).push().setValue(ble);
    }

    public void  updateUserLocation(double lat, double lon){
        firebaseDatabase.getReference().child(userRef).child(uid).child(userLocationRef).setValue(lat+","+lon);
    }

    public MutableLiveData<Boolean> insertUer(User user, String uid){
        firebaseDatabase.getReference().child(userRef).child(uid).push().setValue(user);
        firebaseDatabase.getReference().child(userStatusRef).child(uid).child("status").setValue(false, (error, ref) -> {
            if (error != null){
                Log.d(TAG,error.getMessage());

                isLoggedIn.setValue(false);
            }else {
                isLoggedIn.setValue(true);
            }
        });
        return isLoggedIn;
    }

    public void setBluetoothName(String uid, String name){
        firebaseDatabase.getReference()
                .child("users")
                .child(uid)
                .child("blu_name")
                .setValue(name);
    }

    public MutableLiveData<User> getUpdatedUserMutableLiveData() {

        firebaseDatabase.getReference().child(userRef).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChildren()){

                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.id.equals(uid)){
                        userMutableLiveData.setValue(user);
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
        return userMutableLiveData;
    }
    public MutableLiveData<User> getUserMutableLiveData(String uid) {

        firebaseDatabase.getReference().child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.hasChildren()){

                    User user = snapshot.getValue(User.class);
                    assert user != null;
                    if (!user.id.equals(uid)){
                        userMutableLiveData.setValue(user);
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
        return userMutableLiveData;
    }

    public void getUsersUpdatedStatusMutableLiveData() {

        firebaseDatabase.getReference().child(userStatusRef).addChildEventListener(new ChildEventListener() {
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
//                        firebaseDatabase.getReference(userHistory).child(uid).child(Objects.requireNonNull(snapshot.getKey())).addListenerForSingleValueEvent(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                                Log.d(TAG,"previousChildName " +snapshot);
//                                userStatusAlertMutableLiveData.setValue(new User(snapshot.getKey(), true));
////
//                            }
//
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError error) {
//                                Log.d(TAG,"previousChildName " +error);
//                            }
//                        });

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

    public MutableLiveData<List<History>> getUserHistoryMutableLiveData() {
        List<History> histories = new ArrayList<>();
        firebaseDatabase.getReference().child(userHistory).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot:snapshot.getChildren()) {

                    histories.add(dataSnapshot.getValue(History.class));

                }
                userHistoryMutableLiveData.setValue(histories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return userHistoryMutableLiveData;
    }
    public void checkHistory(String userId){

        firebaseDatabase.getReference().child(userHistory).child(uid).orderByChild("userId").equalTo(userId).limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
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
                                userExist.setValue(true);
                            }else if (history.getDistance() <= 3.5 && history.getTimeSpent() <= 10){
                                userExist.setValue(true);
                            }else if (history.getDistance() <= 5.5 && history.getTimeSpent() <= 15){
                                userExist.setValue(true);
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
    public MutableLiveData<Boolean> getUserExist() {
        return userExist;
    }
    public MutableLiveData<Boolean> getUserStatus() {

        firebaseDatabase.getReference().child(userStatusRef).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userStatus.setValue((Boolean) snapshot.child("status").getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return userStatus;
    }
    public MutableLiveData<User> getUser() {

        firebaseDatabase.getReference().child(userRef).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.hasChildren()){
                    currentUserMutableLiveData.setValue(snapshot.getValue(User.class));
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return currentUserMutableLiveData;
    }
    public MutableLiveData<List<User>> getUsersMutableLiveData() {
        firebaseDatabase.getReference().child(userRef).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot dataSnapshot :
                        snapshot.getChildren()) {
                    userList.add(dataSnapshot.getValue(User.class));
                }
                if (!userList.isEmpty()){
                    usersMutableLiveData.setValue(userList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return usersMutableLiveData;
    }
}


