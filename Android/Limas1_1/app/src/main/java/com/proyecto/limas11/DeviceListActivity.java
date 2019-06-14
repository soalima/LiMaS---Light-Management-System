package com.proyecto.limas11;


import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.widget.ListView;
import android.widget.Toast;

/*********************************************************************************************************
 * Activity que muestra el listado de los dispositivos bluethoot encontrados
 **********************************************************************************************************/

public class DeviceListActivity extends Activity
{
    private ListView mListView;
    private DeviceListAdapter mAdapter;
    private ArrayList<BluetoothDevice> mDeviceList;
    private int posicionListBluethoot;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_paired_devices);

        //defino los componentes de layout
        mListView = (ListView) findViewById(R.id.lv_paired);

        //obtengo por medio de un Bundle del intent la lista de dispositivos encontrados
        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

        //defino un adaptador para el ListView donde se van mostrar en la activity los dispositovs encontrados
        mAdapter = new DeviceListAdapter(this);

        //asocio el listado de los dispositovos pasado en el bundle al adaptador del Listview
        mAdapter.setData(mDeviceList);

        //defino un listener en el boton emparejar del listview
        mAdapter.setListener(listenerBotonEmparejar);
        mListView.setAdapter(mAdapter);

        //se definen un broadcastReceiver que captura el broadcast del SO cuando captura los siguientes eventos:
        IntentFilter filter = new IntentFilter();

        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED); //Cuando se empareja o desempareja el bluethoot

        //se define (registra) el handler que captura los broadcast anterirmente mencionados.
        registerReceiver(mPairReceiver, filter);

    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mPairReceiver);

        super.onDestroy();
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(device, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unpairDevice(BluetoothDevice device) {
        try {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Object[]) null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Metodo que actua como Listener de los eventos que ocurren en los componentes graficos de la activty
    private DeviceListAdapter.OnPairButtonClickListener listenerBotonEmparejar = new DeviceListAdapter.OnPairButtonClickListener() {
        @Override
        public void onPairButtonClick(int position) {
            //Obtengo los datos del dispostivo seleccionado del listview por el usuario
            BluetoothDevice device = mDeviceList.get(position);

            //Se checkea si el sipositivo ya esta emparejado
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                //Si esta emparejado,quiere decir que se selecciono desemparjar y entonces se le desempareja
                unpairDevice(device);
            }
            else
            {

                //Si no esta emparejado,quiere decir que se selecciono emparjar y entonces se le empareja
                showToast("Emparejando");
                posicionListBluethoot = position;
                pairDevice(device);

            }
        }
    };

    //Handler que captura los brodacast que emite el SO al ocurrir los eventos del bluethoot
    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            //Atraves del Intent obtengo el evento de Bluethoot que informo el broadcast del SO
            String action = intent.getAction();

            //si el SO detecto un emparejamiento o desemparjamiento de bulethoot
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                //Obtengo los parametro, aplicando un Bundle, que me indica el estado del Bluethoot
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                //se analiza si se puedo emparejar o no
                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                {
                    //Si se detecto que se puedo emparejar el bluethoot
                    showToast("Emparejado");
                    BluetoothDevice dispositivo = (BluetoothDevice) mAdapter.getItem(posicionListBluethoot);

                    //se inicia el Activity de comunicacion con el bluethoot, para transferir los datos.
                    //Para eso se le envia como parametro la direccion(MAC) del bluethoot Arduino
                    String direccionBluethoot = dispositivo.getAddress();
                    Intent i = new Intent(DeviceListActivity.this, activity_comunicacion.class);
                    i.putExtra("Direccion_Bluethoot", direccionBluethoot);

                    startActivity(i);

                }  //si se detrecto un desaemparejamiento
                else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    showToast("No emparejado");
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };
}


