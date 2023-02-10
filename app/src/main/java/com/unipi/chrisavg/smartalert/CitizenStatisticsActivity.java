package com.unipi.chrisavg.smartalert;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CitizenStatisticsActivity extends AppCompatActivity {

    final static long locationRange = 50000;
    final static long timeRange = 48 * 60 * 60 *  1000;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;
    List<EmergencyAlerts> emergencyAlertsList = new ArrayList<>();
    List<String> ListViewItemsTitle = new ArrayList<>();
    List<String> ListViewItemsDescription = new ArrayList<>();
    List<Integer> ListViewItemsImages = new ArrayList<>();
    Intent intent;

    ArrayAdapterClass arrayAdapterClass;
    ListView listView;
    List<EmergencyAlerts> temp_list;
    Map< String,List<List<EmergencyAlerts>> >AllGroups = new HashMap<>();
    List<List<EmergencyAlerts>> categoryLocationList = new ArrayList<>();
    List<EmergencyAlerts> differentRegionAlerts = new ArrayList<>();
    Map<String, List<EmergencyAlerts>> groupedByCategory;
    SimpleDateFormat formatter;
    Date date;

    Location centreLocation;
    Geocoder geocoder;
    List<Address> addresses= new ArrayList<>();

    LinearLayout linearLayoutPb;
    Map<String,Integer> categoryImagesMap = new HashMap<>();

    TextView emptyView;
    Map<String,String> languageCat;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citizen_statistics);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");
        getSupportActionBar().setTitle(R.string.emergency_alert_stats);

        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

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
                "Earthquake"         ,  R.drawable.earthquake,
                "Extreme Temperature", R.drawable.temperature,
                "Snowstorm"          , R.drawable.snow_storm,
                "Tornado"            , R.drawable.tornado,
                "Storm"              , R.drawable.storm
        );


        linearLayoutPb = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

        listView= (ListView) findViewById(R.id.StatisticsListview);
        emptyView=findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);
        emptyView.setVisibility(View.GONE);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                linearLayoutPb.setVisibility(View.VISIBLE);
                emergencyAlertsList.clear();
                ListViewItemsDescription.clear();
                ListViewItemsTitle.clear();
                ListViewItemsImages.clear();
                AllGroups.clear();

                for(DataSnapshot ds : snapshot.getChildren()) {

                    EmergencyAlerts em = ds.getValue(EmergencyAlerts.class);

                    if(em.getStatus() != null && em.getStatus().equals("Accepted")){
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
        String address;

        double sumX,sumY;
        int countAlerts;




        for ( Map.Entry<String,List<List<EmergencyAlerts>>> entry: AllGroups.entrySet()) {

            for(int i =0;i < entry.getValue().size(); i++) {


                sumX = entry.getValue().get(i).stream().collect(Collectors.summingDouble(x -> x.getLongitude()));
                sumY = entry.getValue().get(i).stream().collect(Collectors.summingDouble(y -> y.getLatitude()));
                countAlerts   = entry.getValue().get(i).size();
                double resX = sumX / countAlerts;
                double resY = sumY / countAlerts;


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
                    address = addresses.get(0).getAddressLine(0);
                }

                formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");
                date=new Date((entry.getValue().get(i).stream().mapToLong(EmergencyAlerts::getTimeStamp).max()).getAsLong());

                String s = getString(R.string.simple_location) + address + "\n"+getString(R.string.time) + formatter.format(date);

                String language=Locale.getDefault().getDisplayLanguage();
                if(language.equals("English")){
                    ListViewItemsTitle.add(entry.getKey());
                }else{
                    ListViewItemsTitle.add(languageCat.get(entry.getKey()));
                }
                ListViewItemsDescription.add(s);
                ListViewItemsImages.add(categoryImagesMap.get(entry.getKey()));

            }
        }

        /*arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,ListViewItems);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();*/

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
        getMenuInflater().inflate(R.menu.actionbar3,menu);
        MenuItem menuItem = menu.findItem(R.id.app_bar_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setQueryHint(getString(R.string.type_to_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                arrayAdapterClass.getFilter().filter(s); //λεμε να κανει αναζητηση στο συγκεκριμενο adapter που εχουμε φτιαξει για το listview
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()) {

            case R.id.Profile:
                finish();
                break;
            case R.id.statistics:
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
