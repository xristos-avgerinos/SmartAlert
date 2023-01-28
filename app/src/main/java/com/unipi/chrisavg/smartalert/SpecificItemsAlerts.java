package com.unipi.chrisavg.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.LauncherApps;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SpecificItemsAlerts extends AppCompatActivity {
    ArrayAdapter arrayAdapter;
    ListView listView;
    List<EmergencyAlerts> list;
    List<String> items;
    List<Address> addresses;
    String address;
    Geocoder geocoder=new Geocoder(this, Locale.getDefault());
    Date date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_specific_items_alerts);
        listView=(ListView) findViewById(R.id.SpecListview);


        list=new ArrayList<>();
        items=new ArrayList<>();

        Intent i=getIntent();
        list = (List<EmergencyAlerts>) i.getSerializableExtra("SpecificItem");



        for (EmergencyAlerts e:list) {

            System.out.println(e.getTitle()+" "+e.getTimeStamp());

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
            date=new Date(e.getTimeStamp());
            items.add("Title: "+e.getTitle()+"\n"+"Location: "+address+"\n"+"Date: "+date+"\n"+"Description: "+e.getDescription());
        }

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,items);
        listView.setAdapter(arrayAdapter);
        arrayAdapter.notifyDataSetChanged();

    }
}