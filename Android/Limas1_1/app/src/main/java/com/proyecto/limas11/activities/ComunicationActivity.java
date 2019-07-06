package com.proyecto.limas11.activities;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.proyecto.limas11.bluetooth.BluetoothConnectionService;
import com.proyecto.limas11.R;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.proyecto.limas11.fragments.BluetoothFragment.MULTIPLE_PERMISSIONS;

/*********************************************************************************************************
 * Activity que tiene la logica de las luces
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity*********************************
public class ComunicationActivity extends Activity {
    Button btnApagar, btnEncender, btnEstadisticaL1, btnEstadisticaL2;
    Switch switchL1, switchL2, swLinterna;
    boolean prendertorch, isflashon;
    static Camera cam = null;
    private String permissions = Manifest.permission.CAMERA;

    private BluetoothConnectionService bluetoothConnection;
    private long lastUpdate;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private static String address = null;
    private BluetoothDevice device;
    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunication);

        //Se definen los componentes del layout
        btnApagar = (Button) findViewById(R.id.btnApagar);
        btnEncender = (Button) findViewById(R.id.btnEncender);

        btnEstadisticaL1 = (Button) findViewById(R.id.btnMNDR8);
        btnEstadisticaL2 = (Button) findViewById(R.id.btnMNDR9);

        switchL1 = (Switch) findViewById(R.id.switchLuz1);
        switchL2 = (Switch) findViewById(R.id.switchLuz2);
        swLinterna = (Switch) findViewById(R.id.swLinterna);

        switchL1.setChecked(false);
        switchL2.setChecked(false);
        swLinterna.setChecked(false);

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino los handlers para los botones Apagar y encender
        btnEncender.setOnClickListener(btnEncenderListener);
        btnApagar.setOnClickListener(btnApagarListener);
        btnEstadisticaL1.setOnClickListener(btnEstadisticaL1Listener);
        btnEstadisticaL2.setOnClickListener(btnEstadisticaL2Listener);

        swLinterna.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    prendertorch = true;
                } else {
                    if (isflashon)
                        off(null);
                    prendertorch = false;
                }
            }
        }));

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(accelerometerSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(proximitySensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(lightSensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
    }

    BroadcastReceiver myReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Acá se recibe el mensaje del arduino y se evalua
            String text = intent.getStringExtra("theMessage");

            Log.d("ComandoRecepcion", text);

            showToast(text);
        }
    };


    @Override
    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        address = extras.getString("Direccion_Bluethoot");
        device = btAdapter.getRemoteDevice(address);
        bluetoothConnection = new BluetoothConnectionService(getApplicationContext());
        //Se declara un receiver para obtener los datos que envíe el embebebido.
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver, new IntentFilter("IncomingMessage"));
        //Si tengo un disposito conectado comienzo la conexión.
        bluetoothConnection.startClient(device, bluetoothConnection.getDeviceUUID());
    }


    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause() {
        super.onPause();

        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Listener del boton encender que envia  msj para enceder Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnEncenderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (switchL1.isChecked()) {
                enviarComando('1');
                //showToast("Encender el LED 1");
            }
            if (switchL2.isChecked()) {
                enviarComando('5');
                //showToast("Encender el LED 2");
            }
        }
    };

    //Listener del boton apagar que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnApagarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (switchL1.isChecked()) {
                enviarComando('2');
                //showToast("Apagar el LED 1");
            }
            if (switchL2.isChecked()) {
                enviarComando('6');
                //showToast("Apagar el LED 2");
            }
        }
    };

    private View.OnClickListener btnEstadisticaL1Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enviarComando('8');
            //showToast("Presionado boton estadistica luz 1");
        }
    };

    private View.OnClickListener btnEstadisticaL2Listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            enviarComando('9');
            //showToast("Presionado boton estadistica luz 2");
        }
    };

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    SensorEventListener lightSensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT && checkPermissions()) {
                long actualTime = event.timestamp;
                if (actualTime - lastUpdate < 1000) {
                    return;
                }
                lastUpdate = actualTime;

                float currentLux = event.values[0];
                if (currentLux < 70 && prendertorch == true) {
                    on(null);
                } else {
                    off(null);
                }
            }
        }
    };

    SensorEventListener accelerometerSensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = event.values;

                float x = values[0];
                float y = values[1];

                float accelationSquareRoot = (x * x + y * y)
                        / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
                long actualTime = event.timestamp;
                if (accelationSquareRoot >= 4) //
                {
                    if (actualTime - lastUpdate < 1000) {
                        return;
                    }
                    lastUpdate = actualTime;
                    if (switchL1.isChecked()) {
                        enviarComando('4');
                    }
                    if (switchL2.isChecked()) {
                        enviarComando('7');
                    }
                }
            }
        }
    };

    SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 3)
                    enviarComando('3');
            }
        }
    };

    public void off(View v) {
        if (isflashon == true) {
            cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
            isflashon = false;
        }
    }

    public void on(View v) {
        if (isflashon == false) {
            cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            isflashon = true;
        }
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }


        result = ContextCompat.checkSelfPermission(ComunicationActivity.this, permissions);
        if (result != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(permissions);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(ComunicationActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    //Método para envíar información al arduino.
    public void enviarComando(char comando) {
        if (device == null) {
            //Si no estoy conectado al bluetooth o se pierde la señal
            // Pido reconectarse
            //showToast("No estás conectado a ningún dispositivo. Conectate vía bluetooth.");
        } else {
            //showToast("Caracter a enviar: " + comando);
            byte[] commandInBytes = String.valueOf(comando).getBytes(Charset.defaultCharset());

            bluetoothConnection.write(commandInBytes);
        }
    }

    @Override
    public void onBackPressed() {
        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.myReceiver);
        } catch (Exception ex) {
        }
        if (device != null) {
            Log.e("[onBACKPRESSED:Limas]", "CANCELANDO THREAD");
            bluetoothConnection.cancel();
        }
        sensorManager.unregisterListener(lightSensorEventListener);
        sensorManager.unregisterListener(proximitySensorEventListener);
        sensorManager.unregisterListener(accelerometerSensorEventListener);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permissions granted.
                    //enableComponent(); // Now you call here what ever you want :)
                } else {
                    String perStr = "";
                    for (String per : permissions) {
                        perStr += "\n" + per;
                    }
                    // permissions list of don't granted permission
                    Toast.makeText(ComunicationActivity.this, "ATENCION: La aplicacion no funcionara " + "correctamente debido a la falta de Permisos", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
