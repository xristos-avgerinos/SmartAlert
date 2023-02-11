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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    EditText email,password;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    String currentToken;
    TextInputLayout passwordLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        passwordLayout = findViewById(R.id.passwordLayout);
        setStatusBarTransparent(this);

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isSuccessful()){
                    currentToken = task.getResult();
                   // System.out.println(currentToken);
                }
            }
        });

        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                passwordLayout.setPasswordVisibilityToggleEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
            email.setError(getString(R.string.email_required));
            email.requestFocus();
        }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches()){
            email.setError(getString(R.string.valid_email));
            email.requestFocus();
        }else if(TextUtils.isEmpty(password.getText().toString())){
            password.setError(getString(R.string.password_required));
            passwordLayout.setPasswordVisibilityToggleEnabled(false);
            password.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(email.getText().toString(),password.getText().toString())
                    .addOnCompleteListener((task)->{
                        if(task.isSuccessful()){

                            reference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users user = snapshot.getValue(Users.class);
                                    String role = user.getRole();
                                    String dbUserToken = user.getToken();
                                    if(dbUserToken==null){
                                        dbUserToken = "";
                                    }
                                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("role", role).apply();

                                    Intent intent ;
                                    if(role.equals("Citizen")){

                                        if(!dbUserToken.equals(currentToken)){
                                            Users temp_user = new Users(user.getFullname(),user.getPhoneNumber(),user.getRole());
                                            temp_user.setToken(currentToken); //update token to database
                                            temp_user.setLatitude(user.getLatitude());
                                            temp_user.setLongitude(user.getLongitude());
                                            reference.child(mAuth.getUid()).setValue(temp_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (!task.isSuccessful()){
                                                        System.out.println("Something went wrong");
                                                    }

                                                }
                                            });
                                        }
                                        intent = new Intent(LoginActivity.this, CitizenProfileActivity.class);

                                    }else if(role.equals("Employee")){
                                        intent = new Intent(LoginActivity.this, EmployeeProfileActivity.class);
                                    }else{
                                        intent=new Intent(LoginActivity.this, LoginActivity.class);
                                        Toast.makeText(LoginActivity.this, getString(R.string.no_role), Toast.LENGTH_SHORT).show();
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
                                showMessage((getString(R.string.error)),getString(R.string.user_not_exists));
                            }catch(Exception e){
                                Log.e(TAG, e.getLocalizedMessage());
                                showMessage((getString(R.string.error)) ,e.getLocalizedMessage());
                            }
                        }
                    });
        }
    }

   //check if user is already logged in.In such case, straightaway take the User to the User's profile activity;
    @Override
    protected void onStart(){
        super.onStart();
        String role = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("role", null);
        if(mAuth.getCurrentUser() != null && role!=null){
            Intent intent = null;
            if(role.equals("Citizen")){
                 intent = new Intent(LoginActivity.this, CitizenProfileActivity.class);
            }else if(role.equals("Employee")){
                intent = new Intent(LoginActivity.this, EmployeeProfileActivity.class);
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