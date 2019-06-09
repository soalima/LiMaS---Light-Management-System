package com.example.limas;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SensorEventListener {

    private SensorManager sensorManager;

    TextView x;
    TextView y;
    TextView z;

    String sx, sy, sz;
    private boolean color = false;
    private View view;
    private long lastUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        x = (TextView) findViewById (R.id.textView2);
        y = (TextView) findViewById (R.id.textView3);
        z = (TextView) findViewById (R.id.textView4);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor (Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor (Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_principal) {
            //setContentView(R.layout.activity_main);
        } else if (id == R.id.nav_bluetooth) {
            //setContentView(R.layout.activity_conectar_bt);
            Intent intent= new Intent(this,ConectarBTActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_control_luz) {

        } else if (id == R.id.nav_config_luz) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
    if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){

        Toast.makeText(this, Float.toString(event.values[0]), Toast.LENGTH_SHORT)
                .show();
    }
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] values = event.values;
            float xVal = event.values[0];
            float yVal = event.values[1];
            float zVal = event.values[2];

            sx = "X Value : <font color = '#800080'> " + xVal + "</font>";
            sy = "Y Value : <font color = '#800080'> " + yVal + "</font>";
            sz = "Z Value : <font color = '#800080'> " + zVal + "</font>";

            x.setText(Html.fromHtml(sx));
            y.setText(Html.fromHtml(sy));
            z.setText(Html.fromHtml(sz));

            float x1 = values[0];
            float y1 = values[1];
            float z1 = values[2];

            float accelationSquareRoot = (x1 * x1 + y1 * y1 + z1 * z1)
                    / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
            long actualTime = event.timestamp;
            if (accelationSquareRoot >= 5) //
            {
                if (actualTime - lastUpdate < 200) {
                    return;
                }
                lastUpdate = actualTime;
                Toast.makeText(this, "Dispositivo sacudido", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
