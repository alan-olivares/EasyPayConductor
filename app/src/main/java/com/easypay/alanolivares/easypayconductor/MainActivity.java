package com.easypay.alanolivares.easypayconductor;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.nav_view);
        bottomNav.setOnNavigationItemSelectedListener(navListener);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contenedor,
                new Cobrar()).commit();
        //I added this if statement to keep the selected fragment when rotating the device

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    switch (item.getItemId()) {
                        case R.id.cobrar:
                            selectedFragment = new Cobrar();
                            break;
                        case R.id.ganancias:
                            selectedFragment = new Ganancias();
                            break;
                        case R.id.historial:
                            selectedFragment = new Historial();
                            break;
                        case R.id.perfil:
                            selectedFragment = new Perfil();
                            break;
                    }

                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_contenedor,
                            selectedFragment).commit();

                    return true;
                }
            };
}
