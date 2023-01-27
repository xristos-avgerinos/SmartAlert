package com.unipi.chrisavg.smartalert;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    EditText email,password;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        setStatusBarTransparent(this);
    }

    private void setStatusBarTransparent(AppCompatActivity activity){
        //Make Status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        //Make status bar icons color dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
    public void GoMainActivity(View view){
        if(TextUtils.isEmpty(email.getText().toString())){
            email.setError("Email is required");
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError("Please enter a valid email");
            email.requestFocus();
        }else if(TextUtils.isEmpty(password.getText().toString())){
            password.setError("Password is required");
            password.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener((task)->{
                        if(task.isSuccessful()){
                            reference.child(mAuth.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String role = snapshot.getValue(String.class);
                                    PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit().putString("role", role).apply();

                                    Intent intent ;
                                    if(role.equals("Citizen")){
                                        intent = new Intent(LoginActivity.this, CitizenProfileActivity.class);
                                    }else if(role.equals("Employee")){
                                        intent = new Intent(LoginActivity.this, AllAlertsActivity.class);
                                    }else{
                                        intent=new Intent(LoginActivity.this, LoginActivity.class);
                                        Toast.makeText(LoginActivity.this, "This user does not have a role.", Toast.LENGTH_SHORT).show();
                                    }
                                    //To prevent User from returning back to Sign Up Activity on pressing back button after registration
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }else {
                            try{
                                throw task.getException();
                            } catch(FirebaseAuthInvalidUserException e){
                                showMessage("Error","User does not exist or is no longer valid.");
                            }catch(Exception e){
                                Log.e(TAG, e.getLocalizedMessage());
                                showMessage("Error",e.getLocalizedMessage());
                            }
                        }
                    });
        }




    }

    //check if user is already logged in.In such case, straightaway take the User to the User's profile activity;
    @Override
    protected void onStart(){
        super.onStart();
        String role = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).getString("role", null);
        if(mAuth.getCurrentUser() != null && role!=null){
            Intent intent = null;
            if(role.equals("Citizen")){
                 intent = new Intent(LoginActivity.this, CitizenProfileActivity.class);
            }else if(role.equals("Employee")){
                intent = new Intent(LoginActivity.this, AllAlertsActivity.class);
            }
            startActivity(intent);
            finish();
        }
    }

    public void GoSignUpActivity(View view){
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }
    public void GoForgetPasswordActivity(View view){
        Intent intent = new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }

}