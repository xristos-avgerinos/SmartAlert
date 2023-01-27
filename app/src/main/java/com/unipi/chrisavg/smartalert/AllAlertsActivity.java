package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.location.Address;
import android.location.Geocoder;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class AllAlertsActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    List<EmergencyAlerts> emergencyAlertsList = new ArrayList<>();
    List<String> ListViewItems = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;
    private ListView listView;
    final static long locationRange = 50000;
    final static long timeRange = 48 * 60 * 60 *  1000;

    List<EmergencyAlerts> temp_list;
    Map< String,List<List<EmergencyAlerts>> >AllGroups = new HashMap<>();

    List<List<EmergencyAlerts>> categoryLocationList = new ArrayList<>();
    List<EmergencyAlerts> differentRegionAlerts = new ArrayList<>();

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



        reference.orderByChild("category").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                EmergencyAlerts emergencyAlert = snapshot.getValue(EmergencyAlerts.class);
                emergencyAlertsList.add(new EmergencyAlerts(emergencyAlert));

                // ListViewItems.add( emergencyAlert.getTitle() +"\n" +  emergencyAlert.getCategory() + "\n" + formatter.format(new Date(emergencyAlert.getTimeStamp())));


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

    public void go2(View view){

        groupedByCategory = emergencyAlertsList.stream().collect(Collectors.groupingBy(w -> w.getCategory()));

        for ( Map.Entry<String,List<EmergencyAlerts>> entry: groupedByCategory.entrySet()) {
            categoryLocationList.clear();
            temp_list = entry.getValue();
            while (temp_list.size()> 0) {

                differentRegionAlerts.clear();

                EmergencyAlerts firstEmergencyAlert=entry.getValue().get(0);

                Location firstLoc = new Location("");
                firstLoc.setLongitude(firstEmergencyAlert.getLongitude());
                firstLoc.setLatitude(firstEmergencyAlert.getLatitude());

                Long firstTimestamp = entry.getValue().get(0).getTimeStamp();
                differentRegionAlerts.add(firstEmergencyAlert);

                for (int i = 1; i < temp_list.size(); i++) {

                    EmergencyAlerts currEmergencyAlert=entry.getValue().get(i);

                    Location itemsLocation = new Location("");
                    itemsLocation.setLongitude(currEmergencyAlert.getLongitude());
                    itemsLocation.setLatitude(currEmergencyAlert.getLatitude());



                    int location_distance = (int) itemsLocation.distanceTo(firstLoc);
                    long time_difference = firstTimestamp - currEmergencyAlert.getTimeStamp();

                    if (location_distance <= locationRange && time_difference <= timeRange) {
                        differentRegionAlerts.add(currEmergencyAlert);
                    }
                }

                temp_list = removeItemsFromList(temp_list,differentRegionAlerts);

                categoryLocationList.add(new ArrayList<>(differentRegionAlerts));


            }
            AllGroups.put(entry.getKey(),new ArrayList<>(categoryLocationList));

        }
        Location centreLocation=new Location("");
        Geocoder geocoder=geocoder = new Geocoder(this, Locale.getDefault());;
        List<Address> addresses=null;
        String address;

        for ( Map.Entry<String,List<List<EmergencyAlerts>>> entry: AllGroups.entrySet()) {

            for(int i =0;i < entry.getValue().size(); i++){


                double sumX = entry.getValue().get(i).stream().collect(Collectors.summingDouble(x-> x.getLongitude()));
                double sumY = entry.getValue().get(i).stream().collect(Collectors.summingDouble(y-> y.getLatitude()));
                int count =entry.getValue().get(i).size();
                double resX=sumX/count;
                double resY=sumY/count;


                centreLocation.setLongitude(resX);
                centreLocation.setLatitude(resY);




                try {
                    addresses = geocoder.getFromLocation(centreLocation.getLatitude(), centreLocation.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω

                address = addresses.get(0).getAddressLine(0);

                System.out.println(entry.getKey()+" "+address);





            }


        }







        /*for (EmergencyAlerts emergencyAlerts : AllGroups.get("Καταιγιδα").get(0)){
            Location location=new Location("");
            location.setLongitude(emergencyAlerts.getLongitude());
            location.setLatitude(emergencyAlerts.getLatitude());



        }*/



        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,ListViewItems);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();


    }

    public List<EmergencyAlerts> removeItemsFromList(List<EmergencyAlerts> Basic_List, List<EmergencyAlerts> Second_List){

        for (EmergencyAlerts ea:Second_List) {
            Basic_List.removeIf(x -> x.getLongitude() == ea.getLongitude() && x.getLatitude() == ea.getLatitude() && x.getTimeStamp()==ea.getTimeStamp());
        }

        return Basic_List;
    }

    void showMessage(String title, String message){
        new AlertDialog.Builder(this).setTitle(title).setMessage(message).setCancelable(true).show();
    }
}