package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class CitizenProfileActivity extends AppCompatActivity /*implements LocationListener*/ {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;

    TextView textViewFullName, textViewEmail, textViewMobile,textView_show_welcome;
    String fullName, email, mobile;
    FusedLocationProviderClient fusedLocationProviderClient;
    //LocationManager locationManager;
    static final int locationRequestCode1 = 111;
    static final int locationRequestCode2 = 123;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_profile);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        getSupportActionBar().setTitle("Citizen Profile");

        textViewFullName = findViewById(R.id.textView_show_full_name);
        textViewEmail = findViewById(R.id.textView_show_email);
        textViewMobile = findViewById(R.id.textView_show_mobile);
        textView_show_welcome = findViewById(R.id.textView_show_welcome);
        //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (user == null) {
            Toast.makeText(this, "Something went wrong! User's details are not available at the moment.", Toast.LENGTH_SHORT).show();
        } else {
            showUserProfile();
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Αν δεν εχω τα permissions τα ζηταω
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, locationRequestCode1);
                return;
            }*/
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

            updateLocationToDB();

        }


    }

    public void AddAlert(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Αν δεν εχω τα permissions τα ζηταω
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode2);
        } else {
            //αν τα εχω τον στελνω κατευθειαν στο επομενο activity
            Intent intent = new Intent(getApplicationContext(), AddAlertActivity.class);
            startActivity(intent);
        }

    }

    private void showUserProfile() {

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
                    textView_show_welcome.setText(new StringBuilder().append("Welcome, ").
                            append(fullName.trim().split("\\s+")[0]).append("!").toString());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == locationRequestCode2) { //ελεγχουμε αν εχει ερθει απο το παραπανω requestPermission με requestCode = 123 που ειναι του AddAlertButton
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                //Αν ο χρηστης πατησει allow τον στελνουμε στο αλλο activity
                Intent intent = new Intent(getApplicationContext(), AddAlertActivity.class);
                startActivity(intent);
            } else {
                //Αν ο χρηστης αρνηθει τα δικαιωματα παραμενω στο activity αυτο και εμφανιζω καταλληλο μηνυμα.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == locationRequestCode1) {//ελεγχουμε αν εχει ερθει απο το παραπανω requestPermission με requestCode = 111 που ειναι του onCreate
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                //Αν ο χρηστης πατησει allow
                Toast.makeText(this, "Permission accepted", Toast.LENGTH_SHORT).show();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                updateLocationToDB();

            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void updateLocationToDB() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Αν δεν εχω τα permissions τα ζηταω
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, locationRequestCode1);
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            reference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Users user = snapshot.getValue(Users.class);
                                    Users temp_user = new Users(user.getFullname(), user.getPhoneNumber(), user.getRole());
                                    temp_user.setToken(user.getToken());
                                    temp_user.setLongitude(location.getLongitude());
                                    temp_user.setLatitude(location.getLatitude());
                                    reference.child(mAuth.getUid()).setValue(temp_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                System.out.println("Something went wrong");
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                    }

                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar3,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.Profile:
                break;
            case R.id.statistics:
                intent = new Intent(getApplicationContext(), CitizenStatisticsActivity.class);
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

/*
    @Override
    public void onLocationChanged(@NonNull Location location) {
        reference.child(mAuth.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                Users temp_user = new Users(user.getFullname(),user.getPhoneNumber(), user.getRole());
                temp_user.setToken(user.getToken());
                temp_user.setLongitude(location.getLongitude());
                temp_user.setLatitude(location.getLatitude());
                reference.child(mAuth.getUid()).setValue(temp_user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()){
                            System.out.println("Something went wrong");
                        }

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        System.out.println(location.getLatitude());
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }*/
}