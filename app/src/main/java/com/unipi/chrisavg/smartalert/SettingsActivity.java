package com.unipi.chrisavg.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class SettingsActivity extends AppCompatActivity {

    Button button;
    RadioButton gr,eng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        gr=findViewById(R.id.grId);
        eng=findViewById(R.id.engId);
        
        




    }
    
    public void setLanguage(View view){

    }
}