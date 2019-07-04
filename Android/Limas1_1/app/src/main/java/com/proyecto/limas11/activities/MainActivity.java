package com.proyecto.limas11.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.proyecto.limas11.R;
import com.proyecto.limas11.fragments.BluetoothFragment;
import com.proyecto.limas11.fragments.ConfiguracionFragment;
import com.proyecto.limas11.fragments.EstadisticasFragment;
import com.proyecto.limas11.fragments.LuzFragment;
import com.proyecto.limas11.fragments.MessageFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,  R.string.navigation_draw_open, R.string.navigation_draw_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if(savedInstanceState == null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MessageFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_message);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_message:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new MessageFragment()).commit();
                break;
            case R.id.nav_bluetooth:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new BluetoothFragment()).commit();
                break;
            case R.id.nav_luz:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new LuzFragment()).commit();
                break;
            case R.id.nav_config:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new ConfiguracionFragment()).commit();
                break;
            case R.id.nav_estadistica:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,new EstadisticasFragment()).commit();
                break;
            case R.id.nav_about:
                Toast.makeText(this, "About sin implementar", Toast.LENGTH_SHORT).show();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed(){
        if(drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
        }
    }

}
