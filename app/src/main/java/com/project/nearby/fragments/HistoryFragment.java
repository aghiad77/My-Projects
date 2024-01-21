package com.project.nearby.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.project.nearby.R;
import com.project.nearby.adapters.HistoryRecyclerViewAdapter;
import com.project.nearby.models.History;
import com.project.nearby.viewModels.FirebaseViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {


    private FirebaseViewModel model;
    HistoryRecyclerViewAdapter  adapter;
    private List<History> historyList = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }


    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        model = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);
        model.getUserHistoryMutableLiveData().observe(getViewLifecycleOwner(), histories -> {

            if (!histories.isEmpty()){
                for (History history : histories){
                    if(historyList.isEmpty()){
                        historyList.add(history);
                    }else{
                        boolean b=false;
                        for(History u2 : historyList){
                            if(u2.getName().equals(history.getName()) && u2.getDistance()==history.getDistance() && u2.getBle_distance()==history.getBle_distance()
                                    && u2.getTimeStamp().equals(history.getTimeStamp())) {
                                b = true;
                                break;
                            }
                        }
                        if(!b){
                            historyList.add(history);
                        }
                    }
                }
                adapter = new HistoryRecyclerViewAdapter(historyList,getContext());
                recyclerView.setAdapter(adapter);
            }
        });
    }
}