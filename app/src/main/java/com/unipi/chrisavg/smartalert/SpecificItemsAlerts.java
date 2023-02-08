package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SpecificItemsAlerts extends AppCompatActivity {
    ListView listView;
    List<EmergencyAlerts> emergencyAlertsList;
    List<String> ListViewItemsTitle;
    List<String> ListViewItemsDescription;
    List<Integer> ListViewItemsImages;
    List<Address> addresses = new ArrayList<>();
    List<Address> centreLocationAddresses = new ArrayList<>();
    String address;
    String centreLocationAddress;
    Geocoder geocoder;
    Date date;
    String description;
    SimpleDateFormat formatter;
    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference referenceUsers;
    EmergencyAlerts tempEmergencyAlert;
    String message;
    String SpecificItemCategory;
    String SpecificItemLongitude;
    String SpecificItemLatitude;
    int SpecificItemImage;
    Location centreLocation;
    final static long locationRange = 50000;
    ArrayAdapterClass arrayAdapterClass;

    List<String> AllUsersTokens = new ArrayList<>();

    LinearLayout linearLayoutPb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_items_alerts);

        linearLayoutPb = (LinearLayout) findViewById(R.id.linlaHeaderProgress);
        linearLayoutPb.setVisibility(View.VISIBLE);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");
        referenceUsers = database.getReference("Users");

        listView=(ListView) findViewById(R.id.SpecListview);
        geocoder = new Geocoder(SpecificItemsAlerts.this, Locale.getDefault());

        emergencyAlertsList =new ArrayList<>();
        ListViewItemsTitle = new ArrayList<>();
        ListViewItemsDescription = new ArrayList<>();
        ListViewItemsImages = new ArrayList<>();

        Intent i=getIntent();
        emergencyAlertsList = (List<EmergencyAlerts>) i.getSerializableExtra("SpecificItemList");
        SpecificItemCategory = i.getStringExtra("SpecificItemCategory");
        SpecificItemLongitude = i.getStringExtra("SpecificItemLongitude");
        SpecificItemLatitude = i.getStringExtra("SpecificItemLatitude");
        SpecificItemImage = i.getIntExtra("SpecificItemImage",R.drawable.appicon);

        getSupportActionBar().setTitle(SpecificItemCategory);

        centreLocation=new Location("");
        centreLocation.setLongitude(Double.parseDouble(SpecificItemLongitude));
        centreLocation.setLatitude(Double.parseDouble(SpecificItemLatitude));

        try {
            centreLocationAddresses = geocoder.getFromLocation(centreLocation.getLatitude(), centreLocation.getLongitude(), 1);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω

        if (centreLocationAddresses.size()==0){
            centreLocationAddress="Untrackable Location";
        }else{
            centreLocationAddress = centreLocationAddresses.get(0).getLocality();
        }


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
            if(e.getDescription().isEmpty()){
                description = "-";
            }else{
                description = e.getDescription();
            }
            ListViewItemsTitle.add("Title: "+e.getTitle());
            ListViewItemsDescription.add("Location: "+address+"\n"+"Date: "+formatter.format(date)+"\n"+"Description: "+description);
            ListViewItemsImages.add(SpecificItemImage);
        }
        linearLayoutPb.setVisibility(View.GONE);
        /*arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ListViewItems);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();*/

        arrayAdapterClass = new ArrayAdapterClass(this, ListViewItemsTitle, ListViewItemsDescription, ListViewItemsImages);

        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();

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

                AllUsersTokens.clear();
                referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot ds : snapshot.getChildren()) {

                            Users user = ds.getValue(Users.class);

                            Location userLocation = new Location("");
                            userLocation.setLongitude(user.getLongitude());
                            userLocation.setLatitude(user.getLatitude());
                            int location_distance = (int) userLocation.distanceTo(centreLocation);

                            if(user.getRole().equals("Citizen") && location_distance <= locationRange){
                                AllUsersTokens.add(user.getToken());
                            }
                        }

                        AllUsersTokens = AllUsersTokens.stream().distinct().collect(Collectors.toList());
                        String[] regIds = new String[AllUsersTokens.size()];
                        AllUsersTokens.toArray(regIds);

                        JSONArray regArray = null;
                        try {
                            regArray = new JSONArray(regIds);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                        FCMsend.sendMessage(regArray,"GR-ALERT: Επείγουσα Ειδοποίηση Εκτακτης Ανάγκης","Προειδοπειητικό μήνυμα για " + SpecificItemCategory + " τις επόμενες ώρες κοντα στην περιοχη " + centreLocationAddress + ". Περιορίστε τις μετακινήσεις στις απολύτως απαραίτητες και ακολουθήστε τις οδηγίες των αρχών. Οδηγίες αυτοπροστασίας: https://www.civilprotection.gr/el/entona-kairika-fainomena.","");

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }

                });
                message = "Emergency Alert was Accepted and notification was sent to close region users";
                Toast.makeText(SpecificItemsAlerts.this, message, Toast.LENGTH_SHORT).show();
                finish();
                startActivity(intent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}