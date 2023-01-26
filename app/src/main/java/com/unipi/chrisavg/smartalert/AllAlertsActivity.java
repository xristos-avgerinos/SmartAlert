package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AllAlertsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    List<EmergencyAlerts> emergencyAlertsList = new ArrayList<>();
    List<String> ListViewItems = new ArrayList<>();
    private ListView listView;
    final static long locationRange = 50000;
    final static long timeRange = 48 * 1000;

    List<EmergencyAlerts> temp_list;
    Map< String,List<List<Location>> >AllGroups = new HashMap<>();

    List<List<Location>> categoryLocationList = new ArrayList<>();
    List<Location> differentRegionAlerts = new ArrayList<>();

    Map<String, List<EmergencyAlerts>> groupedByCategory;
   // HashMap item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_alerts);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");

        listView= (ListView) findViewById(R.id.newListview);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,ListViewItems);
        listView.setAdapter(arrayAdapter);


        reference.orderByChild("category").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                EmergencyAlerts emergencyAlert = snapshot.getValue(EmergencyAlerts.class);
                emergencyAlertsList.add(emergencyAlert);
                ListViewItems.add(emergencyAlert.getTitle() +"\n" +  emergencyAlert.getCategory() + "\n" + formatter.format(new Date(emergencyAlert.getTimeStamp())));

                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });




    }

    public  void go(View view){

        groupedByCategory = emergencyAlertsList.stream().collect(Collectors.groupingBy(w -> w.getCategory()));

        for ( Map.Entry<String,List<EmergencyAlerts>> entry: groupedByCategory.entrySet()) {
            System.out.println(entry.getKey() + " " + entry.getValue());
        }


    }

    public void go2(View view){
        groupedByCategory = emergencyAlertsList.stream().collect(Collectors.groupingBy(w -> w.getCategory()));

        for ( Map.Entry<String,List<EmergencyAlerts>> entry: groupedByCategory.entrySet()) {
            categoryLocationList.clear();
            temp_list = entry.getValue();
            while (temp_list.size()> 0) {

                differentRegionAlerts.clear();
                Location firstLoc = new Location("");
                firstLoc.setLongitude(entry.getValue().get(0).getLongitude());
                firstLoc.setLatitude(entry.getValue().get(0).getLatitude());

                differentRegionAlerts.add(firstLoc);

                for (int i = 1; i < temp_list.size(); i++) {
                    Location itemsLocation = new Location("");
                    itemsLocation.setLongitude(entry.getValue().get(i).getLongitude());
                    itemsLocation.setLatitude(entry.getValue().get(i).getLatitude());

                    int distance = (int) itemsLocation.distanceTo(firstLoc);
                    if (distance <= locationRange) {
                        differentRegionAlerts.add(itemsLocation);
                    }
                }

                temp_list = removeItemsFromList(temp_list,differentRegionAlerts);

                categoryLocationList.add(new ArrayList<>(differentRegionAlerts));


            }
            AllGroups.put(entry.getKey(),new ArrayList<>(categoryLocationList));

        }

        for ( Map.Entry<String,List<List<Location>>> entry: AllGroups.entrySet()) {
            showMessage("",entry.getKey() + " " + entry.getValue());
        }
    }

    List<EmergencyAlerts> removeItemsFromList(List<EmergencyAlerts> Basic_List, List<Location> Second_List){

        for (Location l:Second_List) {
            Basic_List.removeIf(x -> x.getLongitude() == l.getLongitude() && x.getLatitude() == l.getLatitude());
        }

        return Basic_List;
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}