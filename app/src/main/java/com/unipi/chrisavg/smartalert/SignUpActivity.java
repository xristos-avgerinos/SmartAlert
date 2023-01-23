package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    EditText email,fullname,mobilePhone,password,confirmPassword;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        email= findViewById(R.id.et_email);
        fullname=findViewById(R.id.et_username);
        mobilePhone=findViewById(R.id.et_phone);
        password=findViewById(R.id.et_password);
        confirmPassword=findViewById(R.id.et_confirm_password);


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
    }

    private void setStatusBarWhite(AppCompatActivity activity){
        //Make status bar icons color dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }

    public void signUp(View view){
        if(TextUtils.isEmpty(email.getText().toString())){
            Toast.makeText(this, "Please enter your email", Toast.LENGTH_LONG).show();
            email.setError("Email is required");
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            Toast.makeText(this, "Please re-enter your email", Toast.LENGTH_LONG).show();
            email.setError("Valid email is required");
            email.requestFocus();
        }
        else if(TextUtils.isEmpty(fullname.getText().toString())){
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_LONG).show();
            fullname.setError("Full name is required");
            fullname.requestFocus();
        }else if(TextUtils.isEmpty(mobilePhone.getText().toString())){
            Toast.makeText(this, "Please enter your mobile phone number", Toast.LENGTH_LONG).show();
            mobilePhone.setError("Mobile phone number is required");
            mobilePhone.requestFocus();
        }else if(mobilePhone.getText().toString().length() != 10){
            Toast.makeText(this, "Please re-enter your mobile phone number", Toast.LENGTH_LONG).show();
            fullname.setError("Full name is required");
            fullname.requestFocus();
        }
        else if(TextUtils.isEmpty(password.getText().toString())){
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_LONG).show();
            password.setError("Password is required");
            password.requestFocus();
        }
        else if(password.getText().toString().length() < 6){
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show();
            password.setError("Password too weak");
            password.requestFocus();
        }
        else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
            Toast.makeText(this, "Please confirm your password", Toast.LENGTH_LONG).show();
            confirmPassword.setError("Password confirmation is required");
            confirmPassword.requestFocus();
        }else if(!(password.getText().toString()).equals(confirmPassword.getText().toString())){
            Toast.makeText(this, "Please same same password", Toast.LENGTH_LONG).show();
            confirmPassword.setError("Password confirmation is required");
            confirmPassword.requestFocus();
            //Clear the entered passwords
            password.clearComposingText();
            confirmPassword.clearComposingText();

        }
        else{
            mAuth.createUserWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() { //κανουμε ολους τους ελεγχους se αυτον τον listener.γινεται και ελεγχος αν το email ειναι μοναδικο
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                showMessage("Success!","User registered successfully");
                                user = mAuth.getCurrentUser();

                                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                                startActivity(intent);
                                finish();

                            }else {
                                showMessage("Error",task.getException().getLocalizedMessage());
                            }
                        }
                    });
        }

    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}