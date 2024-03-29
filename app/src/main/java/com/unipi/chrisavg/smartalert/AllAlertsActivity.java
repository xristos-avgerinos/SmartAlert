package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.OptionalLong;
import java.util.stream.Collectors;


public class AllAlertsActivity extends AppCompatActivity {

    final static long locationRange = 50000;
    final static long timeRange = 48 * 60 * 60 *  1000;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    List<EmergencyAlerts> emergencyAlertsList = new ArrayList<>();
    List<String> ListViewItemsTitle = new ArrayList<>();
    List<String> ListViewItemsDescription = new ArrayList<>();
    List<Integer> ListViewItemsImages = new ArrayList<>();
    ArrayAdapterClass arrayAdapterClass;

    Map< String[], Integer> ListViewItemsMap = new LinkedHashMap<>();
    List<Map.Entry<String[], Integer>> ListViewItemsList;

    ListView listView;
    List<EmergencyAlerts> temp_list;
    Map< String,List<List<EmergencyAlerts>> >AllGroups = new HashMap<>();
    List<List<EmergencyAlerts>> categoryLocationList = new ArrayList<>();
    List<EmergencyAlerts> differentRegionAlerts = new ArrayList<>();
    Map<String, List<EmergencyAlerts>> groupedByCategory;
    SimpleDateFormat formatter;

    Location centreLocation;
    Geocoder geocoder;
    List<Address> addresses= new ArrayList<>();
    List<String[]> positions;
    Map< String[], Integer> positionMap = new LinkedHashMap<>();
    List<Map.Entry<String[], Integer>> positionList;
    String [] mapIndexes;

    final static long _5hours  = 5 * 60 * 60 *  1000;
    final static long _10hours = 10 * 60 * 60 *  1000;
    final static long _15hours = 15 * 60 * 60 *  1000;
    final static long _24hours = 24 * 60 * 60 *  1000;
    final static long _36hours = 36 * 60 * 60 *  1000;

    LinearLayout linearLayoutPb;
    TextView emptyView;

    Intent intent1;
    Map<String,Integer> categoryImagesMap = new HashMap<>();
    Map<String,String> languageCat;

    String address;
    double sumX,sumY;
    OptionalLong maxTime;
    OptionalLong minTime;
    int countAlerts;
    int dangerForUsers;
    int dangerForTime;
    Integer totalDanger;
    long differenceForMaxMinTime;
    double resX;
    double resY;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_alerts);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");
        getSupportActionBar().setTitle(R.string.emergency_alerts);

        languageCat=new HashMap<>();
        languageCat.put( "Flood"              ,"Πλημμύρα"           );
        languageCat.put( "Fire"               ,"Πυρκαγιά"           );
        languageCat.put( "Earthquake"         ,"Σεισμός"            );
        languageCat.put( "Extreme Temperature","Ακραία Θερμοκρασία" );
        languageCat.put( "Snowstorm"          ,"Χιονοθύελλα"        );
        languageCat.put( "Tornado"            ,"Ανεμοστρόβυλος"     );
        languageCat.put( "Storm"              ,"Καταιγίδα"          );

        categoryImagesMap = Map.of(
                "Flood"              , R.drawable.flood,
                "Fire"               , R.drawable.fire,
                "Earthquake"         , R.drawable.earthquake,
                "Extreme Temperature", R.drawable.temperature,
                "Snowstorm"          , R.drawable.snow_storm,
                "Tornado"            , R.drawable.tornado,
                "Storm"              , R.drawable.storm
                );

        listView= (ListView) findViewById(R.id.SpecListview);
        emptyView=findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);


        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        linearLayoutPb = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutPb.setVisibility(View.VISIBLE);
                emergencyAlertsList.clear();
                ListViewItemsDescription.clear();
                ListViewItemsImages.clear();
                ListViewItemsTitle.clear();
                ListViewItemsMap.clear();
                positionMap.clear();
                AllGroups.clear();

                for(DataSnapshot ds : snapshot.getChildren()) {

                    EmergencyAlerts em = ds.getValue(EmergencyAlerts.class);

                    if(em.getStatus()==null){
                        em.setKey(ds.getKey());
                        emergencyAlertsList.add(em);
                    }
                }
                ShowGroupedEAinListView();
                linearLayoutPb.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });
    }

    public void ShowGroupedEAinListView(){

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

                long firstTimestamp = entry.getValue().get(0).getTimeStamp();
                differentRegionAlerts.add(firstEmergencyAlert);

                for (int i = 1; i < temp_list.size(); i++) {

                    EmergencyAlerts currEmergencyAlert=entry.getValue().get(i);

                    Location itemsLocation = new Location("");
                    itemsLocation.setLongitude(currEmergencyAlert.getLongitude());
                    itemsLocation.setLatitude(currEmergencyAlert.getLatitude());

                    int location_distance = (int) itemsLocation.distanceTo(firstLoc);
                    long time_difference = Math.abs(firstTimestamp - currEmergencyAlert.getTimeStamp());

                    if (location_distance <= locationRange && time_difference <= timeRange) {
                        differentRegionAlerts.add(currEmergencyAlert);
                    }
                }

                temp_list = removeItemsFromList(temp_list,differentRegionAlerts);

                categoryLocationList.add(new ArrayList<>(differentRegionAlerts));

            }
            AllGroups.put(entry.getKey(),new ArrayList<>(categoryLocationList));

        }

        centreLocation=new Location("");
        geocoder = new Geocoder(this, Locale.getDefault());


        for ( Map.Entry<String,List<List<EmergencyAlerts>>> entry: AllGroups.entrySet()) {

            for(int i =0;i < entry.getValue().size(); i++) {


                sumX = entry.getValue().get(i).stream().collect(Collectors.summingDouble(x -> x.getLongitude()));
                sumY = entry.getValue().get(i).stream().collect(Collectors.summingDouble(y -> y.getLatitude()));
                countAlerts   = entry.getValue().get(i).size();
                resX = sumX / countAlerts;
                resY = sumY / countAlerts;


                centreLocation.setLongitude(resX);
                centreLocation.setLatitude(resY);


                try {
                    addresses = geocoder.getFromLocation(centreLocation.getLatitude(), centreLocation.getLongitude(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω

                if (addresses.size()==0){
                    address=getString(R.string.untrackable_location);
                }else{
                    address = addresses.get(0).getLocality();
                }

                //System.out.println(entry.getKey() + " " + address);

                // Βαθμος για αιτήσεις 5/10 απο τον αριθμο των αιτησεων του καθε περιστατικου
                if (countAlerts >= 25){
                    dangerForUsers = 5;
                }else if (countAlerts >= 20){
                    dangerForUsers = 4;
                }else if (countAlerts >= 15){
                    dangerForUsers = 3;
                }else if(countAlerts >= 10){
                    dangerForUsers = 2;
                }else if(countAlerts >= 5){
                    dangerForUsers = 1;
                }else {
                    dangerForUsers = 0;
                }

                //danger= count / 100;

                maxTime = entry.getValue().get(i).stream().mapToLong(EmergencyAlerts::getTimeStamp).max();
                minTime = entry.getValue().get(i).stream().mapToLong(EmergencyAlerts::getTimeStamp).min();
                differenceForMaxMinTime = maxTime.getAsLong() - minTime.getAsLong();

                // Βαθμος για αιτήσεις 5/10 απο την ωρα που υποβληθηκε το πρωτο και το τελευταιο alert για καποιο γεγονος(διαφορα χρονου)
                if(dangerForUsers >= 3){
                    if (differenceForMaxMinTime <= _5hours){
                        dangerForTime = 5;
                    }else if (differenceForMaxMinTime <= _10hours){
                        dangerForTime = 4;
                    }else if (differenceForMaxMinTime <= _15hours){
                        dangerForTime = 3;
                    }else if(differenceForMaxMinTime <= _24hours){
                        dangerForTime = 2;
                    }else if(differenceForMaxMinTime <= _36hours){
                        dangerForTime = 1;
                    }else {
                        dangerForTime = 0;
                    }
                }else{
                    dangerForTime = 0;
                }

                totalDanger = dangerForUsers + dangerForTime; //αποτελεσμα (dangerForUsers + dangerForTime)/10


                /*System.out.println("Min time: "+minTime +"\nMax time:"+maxTime);
                System.out.println("total:" + differenceForMaxMinTime);*/
               // String s = entry.getKey()+" \nΠεριοχή: "+address+"\nΒαθμός Επικυνδυνότητας: "+totalDanger+"/10 \nΜετρητής αιτήσεων:"+countAlerts;
                
                String[] s = new String[]{getString(R.string.simple_location)+ address,getString(R.string.danger)+totalDanger+"/10",getString(R.string.alerts_counter)+countAlerts,
                        String.valueOf(centreLocation.getLongitude()), String.valueOf(centreLocation.getLatitude()),entry.getKey()};
                ListViewItemsMap.put(s,totalDanger);

                mapIndexes= new String[]{entry.getKey(), String.valueOf(i)};
                positionMap.put(mapIndexes,totalDanger);

            }
        }

        positionList = new ArrayList<>(positionMap.entrySet());
        positionList.sort(Map.Entry.comparingByValue (Comparator.reverseOrder()));
        positionMap.clear();
        for (Map.Entry<String[], Integer> posListItem: positionList) {
            positionMap.put(posListItem.getKey(),posListItem.getValue());
        }


        ListViewItemsList = new ArrayList<>(ListViewItemsMap.entrySet());
        ListViewItemsList.sort(Map.Entry.comparingByValue (Comparator.reverseOrder()));
        ListViewItemsMap.clear();
        for (Map.Entry<String[], Integer> posListItem: ListViewItemsList) {
            ListViewItemsMap.put(posListItem.getKey(),posListItem.getValue());
        }


        positions = new ArrayList<>(positionMap.keySet());
        
        
        //ListViewItems = new ArrayList<>(ListViewItemsMap.keySet().iterator().forEachRemaining(key -> String.join(",", key)));
        String language=Locale.getDefault().getDisplayLanguage();

        for (String[] key : ListViewItemsMap.keySet()) {
            ListViewItemsDescription.add(String.join("\n",  Arrays.copyOf(key, key.length - 3)));
            if(language.equals("English")){
                ListViewItemsTitle.add(key[5]);
            }else{
                ListViewItemsTitle.add(languageCat.get(key[5]));
            }

            ListViewItemsImages.add(categoryImagesMap.get(key[5]));

        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView arg0, View arg1, int i, long arg3) {
                /*System.out.println(position.get(i)[0] + pos.get(i)[1]);
                System.out.println(AllGroups.get(pos.get(i)[0]).get(Integer.parseInt(pos.get(i)[1])));*/

                Intent intent = new Intent(AllAlertsActivity.this,SpecificItemsAlerts.class);
                intent.putExtra("SpecificItemList", (Serializable) AllGroups.get(positions.get(i)[0]).get(Integer.parseInt(positions.get(i)[1])));
                intent.putExtra("SpecificItemCategory",(ListViewItemsTitle.get(i)));
                intent.putExtra("SpecificItemLongitude",(new ArrayList<>(ListViewItemsMap.keySet())).get(i)[3]);
                intent.putExtra("SpecificItemLatitude",(new ArrayList<>(ListViewItemsMap.keySet())).get(i)[4]);
                intent.putExtra("SpecificItemImage",(categoryImagesMap.get((new ArrayList<>(ListViewItemsMap.keySet())).get(i)[5])));

                startActivity(intent);
            }
        });
        //arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, ListViewItemsDescription);

        arrayAdapterClass = new ArrayAdapterClass(this, ListViewItemsTitle, ListViewItemsDescription, ListViewItemsImages);

        if (emergencyAlertsList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
        }
        listView.setAdapter(arrayAdapterClass);
        arrayAdapterClass.notifyDataSetChanged();
    }


    public List<EmergencyAlerts> removeItemsFromList(List<EmergencyAlerts> Basic_List, List<EmergencyAlerts> Second_List){

        for (EmergencyAlerts ea:Second_List) {
            Basic_List.removeIf(x -> x.getKey().equals(ea.getKey()));
        }

        return Basic_List;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar4,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.Profile:
                finish();
                break;
            case R.id.EmergencyAlerts:
                break;
            case R.id.logout:
                mAuth.signOut();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().remove("role").apply();
                intent1 = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent1);
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

}