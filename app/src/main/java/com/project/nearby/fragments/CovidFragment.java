package com.project.nearby.fragments;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.core.Constants;
import com.project.nearby.R;
import com.project.nearby.models.History;
import com.project.nearby.models.Message;
import com.project.nearby.utils.Sharedprefs;
import com.project.nearby.viewModels.FirebaseViewModel;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CovidFragment extends Fragment implements View.OnClickListener {

    boolean isChecked;
    FirebaseViewModel model;
    Button submit;
    public static SimpleDateFormat sdf = new SimpleDateFormat("dd / MM / yyyy HH:mm:ss", Locale.ENGLISH);
    public CovidFragment() {
        // Required empty public constructor
    }


    public static CovidFragment newInstance() {

        return new CovidFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_covid, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        submit = view.findViewById(R.id.btn_submit);
        CheckBox checkbox = view.findViewById(R.id.checkbox);
        TextView userName = view.findViewById(R.id.username);
        submit.setOnClickListener(this);
        Sharedprefs sharedprefs = new Sharedprefs(getActivity());
        if (sharedprefs.getStrings("user_name") != null){
            userName.setText("Welcome "+sharedprefs.getStrings("user_name"));
        }

        model = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

        model.getUserStatus().observe(requireActivity(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                checkbox.setChecked(aBoolean);
            }
        });
        checkbox.setOnCheckedChangeListener((compoundButton, b) -> isChecked = b);
    }

    @Override
    public void onClick(View view) {

        submit.setEnabled(false);
        model.setHeathStatus(isChecked);
        if(isChecked)
            SendNotifyToHistoryUser(System.currentTimeMillis() / 1000L);
    }

    private void SendNotifyToHistoryUser(long InfectedDatelong) {

        Date InfectedDate = new Date(InfectedDatelong * 1000L);
        try {
            Date finalInfectedDate = InfectedDate;
            model.getUserHistoryMutableLiveData().observe(getViewLifecycleOwner(), histories -> {

                String alluserid= "";
                if (!histories.isEmpty()) {
                    for (History historyuser : histories) {
                        Date date = null;
                        try {
                            date = sdf.parse(historyuser.getTimeStamp());
                            long diffInMillies = Math.abs(finalInfectedDate.getTime() - date.getTime());
                            long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
                            if(diff <= 14){
                                alluserid = alluserid + historyuser.getUserId() + ";";
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(!alluserid.equals(""))
                        SendMsgToUser(alluserid);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void SendMsgToUser(String alluserId) {
        try {
            model.addMessage(new Message(alluserId,"you have been contacted with infected person in the past weeks",false));
            Toast.makeText(getContext(),"Message has been sent",Toast.LENGTH_LONG).show();
            submit.setEnabled(true);
        } catch (Exception e) {
            Toast.makeText(getContext(),"The message was not sent There is a problem",Toast.LENGTH_LONG).show();
            submit.setEnabled(true);
        }
    }

}