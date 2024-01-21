package com.project.nearby.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.project.nearby.activities.MainActivity;
import com.project.nearby.R;
import com.project.nearby.models.User;
import com.project.nearby.utils.Sharedprefs;
import com.project.nearby.utils.Utils;
import com.project.nearby.viewModels.FirebaseViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = RegisterFragment.class.getSimpleName();
    EditText email;
    EditText userName;
    EditText password;
    TextView login;
    Button btnSignUp;
    private boolean isValidate = true;
    private FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference myRef;
    Sharedprefs sharedprefs;
    private FirebaseViewModel model;
    BluetoothAdapter bluetoothAdapter = null;

    public RegisterFragment() {
        // Required empty public constructor
    }

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sharedprefs = new Sharedprefs(getContext());
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        userName = view.findViewById(R.id.username);
        login = view.findViewById(R.id.login);
        btnSignUp = view.findViewById(R.id.btn_signUp);

        login.setOnClickListener(this);
        btnSignUp.setOnClickListener(this);

        model = new ViewModelProvider(requireActivity()).get(FirebaseViewModel.class);

    }

    private void createUser() {
        database = FirebaseDatabase.getInstance();
        mAuth.createUserWithEmailAndPassword(email.getText().toString().trim(), password.getText().toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        Log.d(TAG, "createUserWithEmail:success");

                        FirebaseUser user = mAuth.getCurrentUser();

                        Log.d(TAG, "createUser: user id is " + user.getUid());

                        User userObj = new User(user.getUid(),userName.getText().toString(), email.getText().toString());
                        model.insertUer(userObj,user.getUid()).observe(getActivity(), aBoolean -> {
                            if (aBoolean){
                                sharedprefs.putBoolean("isLogged", true);
                                sharedprefs.putString("uid", userObj.id);
                                sharedprefs.putString("user_name", userObj.name);
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                                requireActivity().finish();
                                Toast.makeText(getActivity(), "Account create Successfully ", Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(getActivity(), "Account create Unsuccessfully", Toast.LENGTH_SHORT).show();
                            }
                        });


                    } else {

                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            email.setError("Email is already in use");
                        }

                    }

                });
    }

    private boolean validation() {
        isValidate = true;
        if (userName.getText().toString().trim().equals("")) {
            userName.setError("please fill this field");
            isValidate = false;
        }
        if (email.getText().toString().trim().equals("")) {
            email.setError("please fill this field");
            isValidate = false;
        } else if (Utils.isValidEmail(email.getText().toString())) {
            email.setError("Email is not in correct format");
            isValidate = false;
        }
        if (password.getText().toString().trim().equals("")) {
            password.setError("please fill this field");
            isValidate = false;
        } else if (password.getText().toString().length() < 8) {
            password.setError("Password should be greater then 8 characters ");
            isValidate = false;
        }

        return isValidate;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.login) {
            getActivity().getSupportFragmentManager()
                    .popBackStack();
        } else if (view.getId() == R.id.btn_signUp) {
            btnSignUp.setEnabled(false);
            if (validation()) {
                try {
                    createUser();
                }catch (Exception e){
                    Toast.makeText(getContext(),e.toString(),Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    btnSignUp.setEnabled(true);
                }
            }
            btnSignUp.setEnabled(true);
        }
    }
}