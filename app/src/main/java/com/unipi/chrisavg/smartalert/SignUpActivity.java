package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class SignUpActivity extends AppCompatActivity  {

    EditText email,fullname,mobilePhone,password,confirmPassword;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference reference;
    String token;
    LocationManager locationManager;

    TextInputLayout passwordL,confirmPasswordL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        email= findViewById(R.id.et_email);
        fullname=findViewById(R.id.et_fullname);
        mobilePhone=findViewById(R.id.et_phone);
        password=findViewById(R.id.et_password);
        confirmPassword=findViewById(R.id.et_confirm_password);

        passwordL = findViewById(R.id.passwordLayout);
        confirmPasswordL = findViewById(R.id.confPasswordLayout);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // getSupportActionBar().hide(); //hide the title bar
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignUpActivity.super.onBackPressed();
            }
        });
        setStatusBarWhite(this);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    token = task.getResult();
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordL.setPasswordVisibilityToggleEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                confirmPasswordL.setPasswordVisibilityToggleEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void signUp(View view){

        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        if(TextUtils.isEmpty(email.getText().toString())){
            email.setError(getString(R.string.email_required));
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError(getString(R.string.valid_email));
            email.requestFocus();
        }
        else if(TextUtils.isEmpty(fullname.getText().toString())){
            fullname.setError(getString(R.string.fullname_required));
            fullname.requestFocus();
        }else if(TextUtils.isEmpty(mobilePhone.getText().toString())){
            mobilePhone.setError(getString(R.string.mobile_required));
            mobilePhone.requestFocus();
        }else if(mobilePhone.getText().toString().length() != 10){
            mobilePhone.setError(getString(R.string.mobile_length));
            mobilePhone.requestFocus();
        }
        else if(TextUtils.isEmpty(password.getText().toString())){
            password.setError(getString(R.string.password_required));
            passwordL.setPasswordVisibilityToggleEnabled(false);
            password.requestFocus();
        }
        else if(password.getText().toString().length() < 6){
            password.setError(getString(R.string.password_min_length));
            passwordL.setPasswordVisibilityToggleEnabled(false);
            password.requestFocus();
        }
        else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
            confirmPassword.setError(getString(R.string.password_confirmation));
            confirmPasswordL.setPasswordVisibilityToggleEnabled(false);
            confirmPassword.requestFocus();
        }else if(!(password.getText().toString()).equals(confirmPassword.getText().toString())){
            confirmPassword.setError(getString(R.string.passwords_dont_match));
            confirmPasswordL.setPasswordVisibilityToggleEnabled(false);
            confirmPassword.requestFocus();
            //Clear the entered passwords
            password.clearComposingText();
            confirmPassword.clearComposingText();

        }
        else if(!isGPSEnabled){
            showSettingsAlert();
        }
        else{
            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() { //κανουμε ολους τους ελεγχους σε αυτον τον listener.γινεται και ελεγχος αν το email ειναι μοναδικο
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                firebaseUser = mAuth.getCurrentUser();
                                Users user = new Users(fullname.getText().toString(),mobilePhone.getText().toString(),"Citizen");

                                user.setToken(token);
                                reference.child(firebaseUser.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null){

                                            Toast.makeText(SignUpActivity.this, getString(R.string.user_registered), Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(getApplicationContext(), CitizenProfileActivity.class);
                                            //To prevent User from returning back to Sign Up Activity on pressing back button after registration
                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                    | Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivity(intent);
                                            finish();
                                        }else{
                                            Toast.makeText(SignUpActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    }
                                });

                            }else {
                                showMessage(getString(R.string.error),task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }

    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
    public void showSettingsAlert() {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.gps_settings));
        alertDialog.setMessage(R.string.gps_necessary);
        alertDialog.setPositiveButton(R.string.settings, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        alertDialog.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }


}