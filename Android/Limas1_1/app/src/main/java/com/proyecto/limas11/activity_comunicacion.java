package com.proyecto.limas11;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.proyecto.limas11.BluetoothFragment.MULTIPLE_PERMISSIONS;

/*********************************************************************************************************
 * Activity que muestra realiza la comunicacion con Arduino
 **********************************************************************************************************/

//******************************************** Hilo principal del Activity**************************************
public class activity_comunicacion extends Activity implements SensorEventListener {
    Button btnApagar;
    Button btnEncender;
    TextView txtPotenciometro;
    Switch switchL1;
    Switch switchL2;
    Switch swLinterna;
    Boolean switchStateL1;
    Boolean switchStateL2;
    boolean prendertorch;
    boolean isflashon;

    static Camera cam = null;
    private String permissions = Manifest.permission.CAMERA;

    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private long lastUpdate;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
    private static String address = null;

    private SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comunicacion);

        //Se definen los componentes del layout
        btnApagar = (Button) findViewById(R.id.btnApagar);
        btnEncender = (Button) findViewById(R.id.btnEncender);
        txtPotenciometro = (TextView) findViewById(R.id.txtValorPotenciometro);
        switchL1 = (Switch) findViewById(R.id.switchLuz1);
        switchL2 = (Switch) findViewById(R.id.switchLuz2);
        swLinterna=(Switch)findViewById(R.id.swLinterna);

        switchL1.setChecked(false);
        switchL2.setChecked(false);
        swLinterna.setChecked(false);

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        //defino los handlers para los botones Apagar y encender
        btnEncender.setOnClickListener(btnEncenderListener);
        btnApagar.setOnClickListener(btnApagarListener);
        swLinterna.setOnCheckedChangeListener((new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b) {
                    prendertorch = true;
                    //tenia esto pero creo que me equivoque de flag :(
                    //isflashon = true;
                }  else
                {
                    // off(null);
                    if(isflashon)
                        off(null);
                    prendertorch = false;


                }
            }

        }));


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);


    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        address = extras.getString("Direccion_Bluethoot");

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            showToast("La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                finish();
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
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

    /*
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e6) {
            //insert code to deal with this
        }
    }*/

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    private Handler Handler_Msg_Hilo_Principal() {
        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState) {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("\r\n");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0) {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        txtPotenciometro.setText(dataInPrint);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    //Listener del boton encender que envia  msj para enceder Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnEncenderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (switchL1.isChecked()) {
                mConnectedThread.write("1");
                showToast("Encender el LED 1");
            }// Send "1" via Bluetooth
            if (switchL2.isChecked()) {
                mConnectedThread.write("5");
                showToast("Encender el LED 2");
            }
        }
    };

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener btnApagarListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (switchL1.isChecked()) {
                mConnectedThread.write("2");
                showToast("Apagar el LED 1");
            }// Send "1" via Bluetooth
            if (switchL2.isChecked()) {
                mConnectedThread.write("6");
                showToast("Apagar el LED 2");
            }
        }
    };


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //******************************************** Hilo secundario del Activity**************************************
    //*************************************** recibe los datos enviados por el HC05**********************************

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true) {
                try {
                    //se leen los datos del Bluethoot
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                //showToast("La conexion fallo");
                finish();

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] == 3)
                mConnectedThread.write("3");
        }

        if(event.sensor.getType() == Sensor.TYPE_LIGHT){
              /*  mConnectedThread.write("5\n");
                mConnectedThread.write(Integer.toString((int) event.values[0]));
                showToast(Integer.toString((int) event.values[0]));*/
            float currentLux = event.values[0];
            //showToast(Float.toString(currentLux));
            if (currentLux < 50 && prendertorch == true)
            {
                on(null);
            }
            else{
                off(null);
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER && checkPermissions() ) {
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
                if(switchL1.isChecked()){
                    mConnectedThread.write("4");
                    showToast("Cambio estado LED 1");
                }
                if(switchL2.isChecked()){
                    mConnectedThread.write("7");
                    showToast("Cambio estado LED 2");
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void off(View v){
        if(isflashon == true){
            cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            cam.setParameters(p);
            isflashon = false;
        }
    }

    public void on(View v){
        if(isflashon == false){
            cam = Camera.open();
            Camera.Parameters p = cam.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(p);
            isflashon = true;
        }
    }

    private  boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }


            result = ContextCompat.checkSelfPermission(activity_comunicacion.this,permissions);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permissions);
            }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(activity_comunicacion.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),MULTIPLE_PERMISSIONS );
            return false;
        }
        return true;
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
                    Toast.makeText(activity_comunicacion.this, "ATENCION: La aplicacion no funcionara " + "correctamente debido a la falta de Permisos", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
