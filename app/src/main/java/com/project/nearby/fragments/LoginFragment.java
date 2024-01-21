package com.project.nearby.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.project.nearby.activities.MainActivity;
import com.project.nearby.R;
import com.project.nearby.models.User;
import com.project.nearby.utils.Sharedprefs;
import com.project.nearby.utils.Utils;

public class LoginFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = LoginFragment.class.getSimpleName();
    EditText email;
    EditText password;
    CheckBox checkBox;
    TextView register;
    Button btnLogin;
    private FirebaseAuth mAuth;
    private User user;
    private boolean isValidate = true;
    private Sharedprefs sharedprefs;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        sharedprefs = new Sharedprefs(getActivity());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
         return inflater.inflate(R.layout.login_fragment, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        email = view.findViewById(R.id.email);
        password = view.findViewById(R.id.password);
        checkBox = view.findViewById(R.id.checkbox);
        register = view.findViewById(R.id.register);
        btnLogin = view.findViewById(R.id.btn_login);
        register.setOnClickListener(this);
        btnLogin.setOnClickListener(this);
    }

    private void firebaseLogin() {
        try {
            mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser firebaseuser = mAuth.getCurrentUser();
                    assert firebaseuser != null;
                    Log.d(TAG, "_apiLogin: uid is " + firebaseuser.getUid());

                    user = new User();
                    user.id = firebaseuser.getUid();

                    sharedprefs.putBoolean("isLogged",true);
                    sharedprefs.putString("uid",user.id);

                    startActivity(new Intent(getActivity(),MainActivity.class));
                    getActivity().finish();
                }else {
                    Toast.makeText(requireContext(),task.getException().toString(),Toast.LENGTH_LONG).show();
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                        email.setError("Email or password doesn't match");
                    }else if (task.getException() instanceof FirebaseAuthInvalidUserException){
                        email.setError("Email doesn't match");
                    }
                }
            });
        }catch (Exception e){
           e.printStackTrace();
        }
    }

    private boolean validation() {
        isValidate = true;
        if (email.getText().toString().trim().equals("")) {
            email.setError("please fill this field");
            isValidate = false;
        }else if (Utils.isValidEmail(email.getText().toString())) {
            email.setError("Email is not in correct format");
            isValidate = false;
        }
        if (password.getText().toString().trim().equals("")) {
            password.setError("please fill this field");
            isValidate = false;
        }else if(password.getText().toString().length() < 8) {
            password.setError("Password should be greater then 8 characters ");
            isValidate = false;
        }

        return isValidate;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.register) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, RegisterFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        } else if (view.getId() == R.id.btn_login) {
            //if (validation()) {
                firebaseLogin();
            //}
        }
    }
}