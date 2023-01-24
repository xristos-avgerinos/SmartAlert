package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EmployeeProfileActivity extends AppCompatActivity {
FirebaseAuth mAuth;
FirebaseDatabase database;
FirebaseUser user;
DatabaseReference reference;
TextView  textViewFullName, textViewEmail, textViewMobile;
String fullName, email, mobile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");



        getSupportActionBar().setTitle("Employee Profile");

        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewMobile = findViewById(R.id.textView_show_mobile);

        if(user == null){
            Toast.makeText(this, "Something went wrong! User's details are not available at the moment.", Toast.LENGTH_SHORT).show();
        }else{
            showEmployeeProfile();
        }
    }


    private void showEmployeeProfile () {

        //Extracting User Reference from Database for "Registered Users"
        reference.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users userDetails = snapshot.getValue(Users.class);
                if (userDetails != null) {
                    email = user.getEmail();
                    fullName = userDetails.getFullname();
                    mobile = userDetails.getPhoneNumber();

                    textViewFullName.setText(fullName);
                    textViewEmail.setText(email);
                    textViewMobile.setText(mobile);
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){

            }
        });
    }
}