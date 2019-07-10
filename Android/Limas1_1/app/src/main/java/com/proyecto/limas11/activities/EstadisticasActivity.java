package com.proyecto.limas11.activities;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.proyecto.limas11.R;

public class EstadisticasActivity extends AppCompatActivity {
    Button btnVolver;
    TextView tvUsoLed1,tvUsoLed2,tvRestante1,tvRestante2;
    private final static  Double vida = 3600.0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);
        tvUsoLed1 = (TextView) findViewById(R.id.tvUsoLed1);
        tvUsoLed2 = (TextView) findViewById(R.id.tvUsoLed2);
        tvRestante1= (TextView) findViewById(R.id.tvRestanteLed1);
        tvRestante2 = (TextView) findViewById(R.id.tvRestanteLed2);
        btnVolver = (Button) findViewById(R.id.btnVolver);

        btnVolver.setOnClickListener(btnVolverListener);

        String led1;
        String led2;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                led1 = "0";
                led2 = "0";
            } else {
                led1 = extras.getString("LED1");
                led2 = extras.getString("LED2");
            }
        } else {
            led1 = (String) savedInstanceState.getSerializable("LED1");
            led2 = (String) savedInstanceState.getSerializable("LED2");
        }
        if(led1 != null && !led1.isEmpty()) {
            Double numero1 = Double.parseDouble(led1);
            Double restante1 = vida - numero1;
            tvRestante1.setText(Double.toString(restante1));
            tvUsoLed1.setText(led1);
        }
        else{
            tvRestante1.setText(Double.toString(vida));
            tvUsoLed1.setText("0");
            }
        if(led2 != null && !led2.isEmpty()) {
            Double numero2 = Double.parseDouble(led2);
            Double restante2 = vida - numero2;
            tvRestante2.setText(Double.toString(restante2));
            tvUsoLed2.setText(led2);
        }
        else{
            tvRestante2.setText(Double.toString(vida));
            tvUsoLed2.setText("0");
        }






    }

    private View.OnClickListener btnVolverListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        finish();
        }
    };
}
