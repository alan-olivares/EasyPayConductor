package com.easypay.alanolivares.easypayconductor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.Toast;

public class Inicio extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                    /* Create an Intent that will start the Menu-Activity. */
                Intent mainIntent = new Intent(Inicio.this,LoginActivity.class);
                Inicio.this.startActivity(mainIntent);
                Inicio.this.finish();
            }
            }, 1000);


    }
}
