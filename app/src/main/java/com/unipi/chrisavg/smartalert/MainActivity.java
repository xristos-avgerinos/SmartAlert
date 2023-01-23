package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    FloatingActionButton floatingActionButton;
    static final int locationRequestCode = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        floatingActionButton = findViewById(R.id.floatingActionButton);
    }
    public void floatingActionButtonClick(View view){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Αν δεν εχω τα permissions τα ζηταω
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},locationRequestCode);
        }
        else{
            //αν τα εχω τον στελνω κατευθειαν στο επομενο activity
            Intent intent = new Intent(getApplicationContext(),MainActivity2.class);
            startActivity(intent);
        }

    }

    

    private void changeUserProfile(String displayName, String imageUrl, FirebaseUser user){ //μεθοδος γιαν  κανουμε update καποια στοιχεια του χρηστη
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .setPhotoUri(Uri.parse(imageUrl))
                .build();
        user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(MainActivity.this, "User profile updated!", Toast.LENGTH_SHORT).show();
                else showMessage("Error",task.getException().getLocalizedMessage());
            }
        });
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
    public void write(View view){//με αυτο το write γραφουμε κατω απο το Users για να μην το βλεπουν ολοι παρα μονο ο συγκεκριμενος χρηστης
        Map<String,String> hashmap = new HashMap<>();
        hashmap.put("display name", "disp");
        reference.child(mAuth.getUid()).setValue(hashmap);
    }
    public void read(View view){
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                showMessage("DB data change", snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) { //ελεγχουμε αν εχει ερθει απο το παραπανω requestPermission με requestCode = 123
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Αν ο χρηστης πατησει allow τον στελνουμε στο αλλο activity
                Intent intent = new Intent(getApplicationContext(),MainActivity2.class);
                startActivity(intent);
            } else {
                //Αν ο χρηστης αρνηθει τα δικαιωματα παραμενω στο activity αυτο και εμφανιζω καταλληλο μηνυμα.
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }

    }

}