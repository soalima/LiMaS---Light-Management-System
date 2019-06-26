package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Switch switchL1;
    Switch switchL2;
    Boolean switchStateL1;
    Boolean switchStateL2;
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchL1 = (Switch) findViewById(R.id.switch1);
        switchL2 = (Switch) findViewById(R.id.switch2);
        btn = (Button) findViewById(R.id.button);

        switchStateL1 = switchL1.isChecked();
        switchStateL2 = switchL2.isChecked();

        switchL1.setChecked(false);
        switchL2.setChecked(false);

        btn.setOnClickListener(btnListener);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (switchL1.isChecked()) {
                //mConnectedThread.write("1");
                showToast("Encender el LED 1");
            }// Send "1" via Bluetooth
            if (switchL2.isChecked()) {
                //mConnectedThread.write("5");
                showToast("Encender el LED 2");
            }
        }
    };

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
