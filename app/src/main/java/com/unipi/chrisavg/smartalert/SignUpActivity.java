package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {

    EditText email,fullname,mobilePhone,password,confirmPassword;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase database;
    DatabaseReference reference;

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
            email.setError("Email is required");
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError("Please enter a valid email");
            email.requestFocus();
        }
        else if(TextUtils.isEmpty(fullname.getText().toString())){
            fullname.setError("Full name is required");
            fullname.requestFocus();
        }else if(TextUtils.isEmpty(mobilePhone.getText().toString())){
            mobilePhone.setError("Mobile phone number is required");
            mobilePhone.requestFocus();
        }else if(mobilePhone.getText().toString().length() != 10){
            mobilePhone.setError("mobile phone number must be 10 characters");
            mobilePhone.requestFocus();
        }
        else if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("Password is required");
            password.requestFocus();
        }
        else if(password.getText().toString().length() < 6){
            password.setError("Password must be at least 6 characters");
            password.requestFocus();
        }
        else if(TextUtils.isEmpty(confirmPassword.getText().toString())){
            confirmPassword.setError("Password confirmation is required");
            confirmPassword.requestFocus();
        }else if(!(password.getText().toString()).equals(confirmPassword.getText().toString())){
            confirmPassword.setError("The passwords do not match");
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
                                firebaseUser = mAuth.getCurrentUser();

                                Users user = new Users(fullname.getText().toString(),mobilePhone.getText().toString(),"Citizen");
                                reference.child(firebaseUser.getUid()).setValue(user, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        if (error == null){

                                            Toast.makeText(SignUpActivity.this, "User registered successfully", Toast.LENGTH_SHORT).show();

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