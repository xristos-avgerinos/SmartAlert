package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
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
TextView  textViewFullName, textViewEmail, textViewMobile,textView_show_welcome;
String fullName, email, mobile;

Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_profile);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");



        getSupportActionBar().setTitle(R.string.employee_profile);

        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewMobile = findViewById(R.id.textView_show_mobile);

        textView_show_welcome = findViewById(R.id.textView_show_welcome);

        if(user == null){
            Toast.makeText(this, getString(R.string.unavailable_details), Toast.LENGTH_SHORT).show();
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
                    textView_show_welcome.setText(new StringBuilder().append(getString(R.string.welcome)).
                            append(fullName.trim().split("\\s+")[0]).append("!").toString());
                }
            }

            @Override
            public void onCancelled (@NonNull DatabaseError error){

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar4,menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        menuItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.Profile:
                break;
            case R.id.EmergencyAlerts:
                intent = new Intent(getApplicationContext(), AllAlertsActivity.class);
                startActivity(intent);
                break;

            case R.id.logout:
                mAuth.signOut();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("role").apply();
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}