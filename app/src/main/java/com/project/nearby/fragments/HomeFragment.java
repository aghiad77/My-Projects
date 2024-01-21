package com.project.nearby.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.project.nearby.R;
import com.project.nearby.activities.MainActivity;
import com.project.nearby.adapters.NeighbourRecyclerViewAdapter;
import com.project.nearby.models.User;
import com.project.nearby.utils.Notification;
import com.project.nearby.utils.Sharedprefs;
import com.project.nearby.utils.Utils;
import com.project.nearby.viewModels.FirebaseViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private FirebaseViewModel model;
    RecyclerView recyclerView;
    NeighbourRecyclerViewAdapter adapter;
    List<User> userList = new ArrayList<>();
    TextView userName;
    String uid;
    double lat;
    double lon;
    Sharedprefs sharedprefs;
    String Get_Neighbours="ACTION_GET_NEIGHBOURS";
    private BroadcastReceiver refreshNeighbours = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String userId = intent.getStringExtra("userId");
            final String status = intent.getStringExtra("status");
            if(status.equals("user")) {
                Boolean isExist = false;
                for (User user : userList) {
                    if (user.id.equals(userId))
                        isExist = true;
                }
                if (!isExist)
                    refreshNeighboursList(userId);
            }else{
                checkNeighboursStatus(userId);
            }
        }
    };

    public HomeFragment() {
        // Required empty public constructor
        uid = FirebaseAuth.getInstance().getUid();
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedprefs= new Sharedprefs(requireContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_scan).setOnClickListener(this);
        recyclerView = view.findViewById(R.id.neighbourRecyclerView);
        userName = view.findViewById(R.id.username);
        model = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        if (sharedprefs.getStrings("user_name") != null){
            userName.setText("Hello "+sharedprefs.getStrings("user_name"));
        }else {
            model.getUser().observe(getActivity(), user -> {
                if (user != null){
                    userName.setText("Hello "+user.name);
                    sharedprefs.putString("user_name",user.name);
                }
            });
        }

        if (getActivity() != null){
            getUsers();
        }
    }


    @Override
    public void onClick(View view) {
        //getUsers();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(refreshNeighbours, new IntentFilter(Get_Neighbours));
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(refreshNeighbours);
        super.onPause();
    }

    private void getUsers() {
        model.getUsersMutableLiveData().observe(requireActivity(), users -> {
            List<User> userRemoveList = new ArrayList<>();
            for (User user : users) {
                if (user.id != null &&!user.id.equals("") && !user.latlon.equals("") ){
                    if (user.id.equals(uid)){
                        lat = Double.parseDouble(user.latlon.split(",")[0]);
                        lon = Double.parseDouble(user.latlon.split(",")[1]);
                    }else {
                        userList.add(user);
                    }
                }
            }
            for (User user : userList) {
                if (!user.latlon.equals("")){
                    double distance = Utils.getDistance(lat,lon,Double.parseDouble(user.latlon.split(",")[0]),Double.parseDouble(user.latlon.split(",")[1]) );
                    if (distance > 100){
                        userRemoveList.add(user);
                    }
                }else {
                    userRemoveList.add(user);
                }
            }
            userList.removeAll(userRemoveList);
            adapter = new NeighbourRecyclerViewAdapter(userList, requireActivity());
            recyclerView.setAdapter(adapter);
        });

    }

    private void refreshNeighboursList(String id) {
        model.getUsersMutableLiveData().observe(requireActivity(), users -> {
            for (User user : users) {
                if (user.id != null && user.id.equals(id)){
                    if(!userList.contains(user))
                        userList.add(user);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void checkNeighboursStatus(String id){
        List<User> newList=userList;
        Boolean Exist=false;
        for(User user:newList){
            if(user.id.equals(id)){
                user.setStatus(true);
                Exist=true;
            }
        }
        if(Exist){
            new Notification(getContext()).buildNotification("There is a covid positive in your neighbours list");
            userList.clear();
            userList.addAll(newList);
            adapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        model.getUsersMutableLiveData().removeObservers(requireActivity());
    }
}