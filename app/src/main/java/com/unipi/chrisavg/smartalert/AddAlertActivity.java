package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AddAlertActivity extends AppCompatActivity implements LocationListener {
    EditText titleEditText;
    EditText timestampEditText;
    EditText locationEditText;
    EditText descriptionEditText;
    Spinner dropdown;
    LocationManager locationManager;

    FirebaseDatabase database;
    DatabaseReference reference;

    Location locationForModel;
    Date dateForModel;
    ProgressBar progressBar;
    Map<String,String> languageCat;
    String[] items;
    ArrayAdapter<String> adapter;
    SimpleDateFormat formatter;

    Intent intent;
    static final int LOCATION_SETTINGS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);


        titleEditText = findViewById(R.id.titleEditText);
        timestampEditText = findViewById(R.id.timestampEditText);
        locationEditText = findViewById(R.id.locationEditText);
        dropdown = findViewById(R.id.spinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        progressBar = findViewById(R.id.progressBar);

        languageCat=new HashMap<>();
        languageCat.put( "Πλημμύρα"          ,"Flood"               );
        languageCat.put( "Πυρκαγιά"          ,"Fire"                );
        languageCat.put( "Σεισμός"           ,"Earthquake"          );
        languageCat.put( "Ακραία Θερμοκρασία","Extreme Temperature" );
        languageCat.put( "Χιονοθύελλα"       ,"Snowstorm"           );
        languageCat.put( "Ανεμοστρόβυλος"    ,"Tornado"             );
        languageCat.put( "Καταιγίδα"         ,"Storm"               );



        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");

        //φτιαχνω εναν adapter με τα στοιχεια της λιστας  items και το περναω στο dropdown spinner
        items = new String[]{getString(R.string.select_alert_category),getString(R.string.flood), getString(R.string.fire), getString(R.string.earthquake), getString(R.string.extreme_temperature),getString(R.string.snowstorm),getString(R.string.tornado),getString(R.string.storm)};
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items){
            @Override
            public boolean isEnabled(int position){
                // Disable the first item from Spinner
                // First item will be use for hint
                return position != 0;
            }
            @Override
            public View getDropDownView(
                    int position, View convertView,
                    @NonNull ViewGroup parent) {

                // Get the item view
                View view = super.getDropDownView(
                        position, convertView, parent);
                TextView textView = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    textView.setTextColor(Color.GRAY);
                }
                else { textView.setTextColor(Color.BLACK); }
                return view;
            }
        };


        dropdown.setAdapter(adapter);

        getSupportActionBar().setTitle(R.string.new_emergency_alert);
        formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        dateForModel = new Date();
        timestampEditText.setText(formatter.format(dateForModel));

        timestampEditText.setKeyListener(null);
        locationEditText.setKeyListener(null);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //κανω εναν τυπικο ελεγχο αν εχω τα permissions αν και για να εχω φτασει σε αυτο το activity ο χρηστης εχει αποδεχτει τα permissions
            finish();
        }else{

            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if(!isGPSEnabled){ //αν δεν εχει ανοιξει το location στο κινητο του τοτε τον στελνω στα settings αν θελει ωστε να το ανοιξει και να παρω την τοποθεσια του
                showSettingsAlert();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,this);
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.gps_settings);
        alertDialog.setMessage(R.string.settings_menu);
        alertDialog.setPositiveButton(R.string.settings, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent,LOCATION_SETTINGS_REQUEST);

        });
        alertDialog.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        alertDialog.show();
    }

    public void showMessage(String title, String text){
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar1,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        intent  = new Intent(getApplicationContext(), CitizenProfileActivity.class);
        switch(item.getItemId()) {
            case R.id.CancelButton:
                finish();
                startActivity(intent);
                break;
            case R.id.SaveButton:
                if (locationEditText.getText().toString().isEmpty()){ //δεν μπορει να γινει αποθηκευση του alert αν δεν εχει παρθει το location του χρηστη αυτοματα
                    showMessage(getString(R.string.no_gps_connection),getString(R.string.gps_loading));
                }
                else if (titleEditText.getText().toString().trim().isEmpty() ){
                    showMessage(getString(R.string.simple_title),getString(R.string.please_give_a_title));
                }else if(dropdown.getSelectedItemPosition() == 0){
                    showMessage(getString(R.string.simple_category), getString(R.string.select_category));
                }
                else{
                    //Save category only in english locale
                    String category;
                    if(languageCat.containsKey(dropdown.getSelectedItem().toString())){
                        category =languageCat.get(dropdown.getSelectedItem().toString());
                    }else{
                        category=dropdown.getSelectedItem().toString();
                    }

                    EmergencyAlerts emergencyAlerts = new EmergencyAlerts(titleEditText.getText().toString(), dateForModel.getTime() ,
                            locationForModel.getLatitude(),locationForModel.getLongitude(),
                            locationEditText.getText().toString(),category,descriptionEditText.getText().toString());

                    reference.push().setValue(emergencyAlerts, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null){
                                Toast.makeText(AddAlertActivity.this, getString(R.string.alert_reported), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(AddAlertActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
                    finish();
                    startActivity(intent);
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω
        String address;
        if (addresses.size()!=0){
            address = addresses.get(0).getAddressLine(0);
            locationEditText.setText(address);
            locationForModel = location;
            progressBar.setVisibility(View.GONE);
            locationManager.removeUpdates(this);

        }

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }
    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST) {
            // user is back from location settings
            finish();
            startActivity(getIntent()); //reload activity to get location

        }
    }



}