package com.unipi.chrisavg.smartalert;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setStatusBarTransparent(this);
    }

    private void setStatusBarTransparent(AppCompatActivity activity){
        //Make Status bar transparent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        //Make status bar icons color dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            activity.getWindow().setStatusBarColor(Color.WHITE);
        }
    }
    public void GoMainActivity(View view){
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    public void GoSignUpActivity(View view){
        Intent intent = new Intent(this,SignUpActivity.class);
        startActivity(intent);
    }
    public void GoForgetPasswordActivity(View view){
        Intent intent = new Intent(this,ForgotPasswordActivity.class);
        startActivity(intent);
    }

}