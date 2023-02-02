package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SpecificItemsAlerts extends AppCompatActivity {
    ArrayAdapter arrayAdapter;
    ListView listView;
    List<EmergencyAlerts> emergencyAlertsList;
    List<String> ListViewItems;
    List<Address> addresses;
    String address;
    Geocoder geocoder;
    Date date;
    SimpleDateFormat formatter;
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference referenceUsers;
    EmergencyAlerts tempEmergencyAlert;
    String message;

    List<String> AllUsersTokens = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_items_alerts);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");
        referenceUsers = database.getReference("Users");

        listView=(ListView) findViewById(R.id.SpecListview);
        geocoder = new Geocoder(SpecificItemsAlerts.this, Locale.getDefault());

        emergencyAlertsList =new ArrayList<>();
        ListViewItems =new ArrayList<>();

        Intent i=getIntent();
        emergencyAlertsList = (List<EmergencyAlerts>) i.getSerializableExtra("SpecificItem");



        for (EmergencyAlerts e: emergencyAlertsList) {

            try {
                addresses = geocoder.getFromLocation(e.getLatitude(), e.getLongitude(), 1);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω

            if (addresses.size()==0){
                address="Untrackable Location";
            }else{
                address = addresses.get(0).getAddressLine(0);
            }
            formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
            date=new Date(e.getTimeStamp());
            ListViewItems.add("Title: "+e.getTitle()+"\n"+"Location: "+address+"\n"+"Date: "+formatter.format(date)+"\n"+"Description: "+e.getDescription());
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ListViewItems);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar2,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = new Intent(getApplicationContext(), AllAlertsActivity.class);

        switch(item.getItemId()) {
            case R.id.DeclineButton:
                for (EmergencyAlerts e: emergencyAlertsList) {
                        e.setStatus("Declined");
                        String key = e.getKey();
                        tempEmergencyAlert = new EmergencyAlerts(e.getTitle(),e.getTimeStamp(),e.getLatitude(),e.getLongitude(),e.getCategory(),e.getDescription());
                        tempEmergencyAlert.setStatus(e.getStatus());
                        reference.child(key).setValue(tempEmergencyAlert).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()){
                                    System.out.println("Something went wrong");
                                }
                            }

                        });

                }
                message = "Emergency Alert was Declined";
                Toast.makeText(SpecificItemsAlerts.this, message, Toast.LENGTH_SHORT).show();
                finish();
                startActivity(intent);
                break;
            case R.id.AcceptButton:
                for (EmergencyAlerts e: emergencyAlertsList) {
                    e.setStatus("Accepted");
                    String key = e.getKey();
                    tempEmergencyAlert = new EmergencyAlerts(e.getTitle(),e.getTimeStamp(),e.getLatitude(),e.getLongitude(),e.getCategory(),e.getDescription());
                    tempEmergencyAlert.setStatus(e.getStatus());
                    reference.child(key).setValue(tempEmergencyAlert).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                System.out.println("Something went wrong");
                            }

                        }
                    });

                }
                message = "Emergency Alert was Accepted";
                Toast.makeText(SpecificItemsAlerts.this, message, Toast.LENGTH_SHORT).show();
                finish();
                startActivity(intent);
                break;
            case R.id.test:
                AllUsersTokens.clear();
                referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()) {

                            Users user = ds.getValue(Users.class);

                            if(user.getRole().equals("Citizen")){
                                AllUsersTokens.add(user.getToken());
                            }
                        }
                        String[] regIds = new String[AllUsersTokens.size()];
                        AllUsersTokens.toArray(regIds);

                        JSONArray regArray = null;
                        try {
                            regArray = new JSONArray(regIds);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        FCMsend.sendMessage(regArray,"Hello","How r u","My Name is Vishal");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });




               break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}