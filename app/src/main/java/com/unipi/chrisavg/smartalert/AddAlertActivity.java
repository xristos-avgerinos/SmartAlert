package com.unipi.chrisavg.smartalert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_alert);

        titleEditText = findViewById(R.id.titleEditText);
        timestampEditText = findViewById(R.id.timestampEditText);
        locationEditText = findViewById(R.id.locationEditText);
        dropdown = findViewById(R.id.spinner);
        descriptionEditText = findViewById(R.id.descriptionEditText);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Emergency Alerts");

        //φτιαχνω εναν adapter με τα στοιχεια της λιστας  items και το περναω στο dropdown spinner
        String[] items = new String[]{"Πλημμυρα", "Πυρκαγια", "Σεισμος", "Ακραια θερμοκασια","Χιονοθυελα","Ανεμοστροβυλος","Καταιγιδα"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        getSupportActionBar().setTitle("New Emergency Alert");
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
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
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0,0,this);
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });
        alertDialog.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
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
        Intent intent = new Intent(getApplicationContext(), CitizenProfileActivity.class);
        switch(item.getItemId()) {
            case R.id.CancelButton:
                finish();
                startActivity(intent);
                break;
            case R.id.SaveButton:
                if (locationEditText.getText().toString().isEmpty()){ //δεν μπορει να γινει αποθηκευση του alert αν δεν εχει παρθει το location του χρηστη αυτοματα
                    showMessage("No GPS connection","Please wait until you find GPS connection so as we can access your location!");
                }
                else if (titleEditText.getText().toString().trim().isEmpty() ){
                    showMessage("Give title","Give your a title!");
                }
                else{
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    EmergencyAlerts emergencyAlerts = new EmergencyAlerts(titleEditText.getText().toString(), dateForModel.getTime() ,
                            locationForModel.getLatitude(),locationForModel.getLongitude(),dropdown.getSelectedItem().toString(),descriptionEditText.getText().toString());
                    reference.push().setValue(emergencyAlerts, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                            if (error == null){
                                Toast.makeText(AddAlertActivity.this, "Alert Emergency was added to database", Toast.LENGTH_SHORT).show();
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
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        //απο τις συντεταγμενες latitude και longitude παιρνω την διευθνυση του και οτι αλλη πληροφορια θελω

        String address = addresses.get(0).getAddressLine(0);
        /*String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String zip = addresses.get(0).getPostalCode();
        String country = addresses.get(0).getCountryName();*/
        locationEditText.setText(new StringBuilder().append("Latitude: ").append(location.getLatitude()).append("\nLongitude: ").append(location.getLongitude())
                .append("\n").append(address));
        locationForModel = location;
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

    }



}