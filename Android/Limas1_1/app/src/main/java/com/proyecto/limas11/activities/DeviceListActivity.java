package com.proyecto.limas11.activities;


import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.widget.ListView;
import android.widget.Toast;

import com.proyecto.limas11.AlmacenGlobal;
import com.proyecto.limas11.adapters.DeviceListAdapter;
import com.proyecto.limas11.R;


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
        mListView = (ListView) findViewById(R.id.lv_paired);
        mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");
        mAdapter = new DeviceListAdapter(this);
        mAdapter.setData(mDeviceList);
        mAdapter.setListener(listenerBotonEmparejar);
        mListView.setAdapter(mAdapter);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
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

            SharedPreferences settings = getSharedPreferences("preferenciasFile", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("addr", null);
            editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DeviceListAdapter.OnPairButtonClickListener listenerBotonEmparejar = new DeviceListAdapter.OnPairButtonClickListener() {
        @Override
        public void onPairButtonClick(int position) {
            BluetoothDevice device = mDeviceList.get(position);
            if (device.getBondState() == BluetoothDevice.BOND_BONDED)
            {
                unpairDevice(device);
            }
            else
            {
                posicionListBluethoot = position;
                pairDevice(device);

            }
        }
    };

    private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action))
            {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                {

                    BluetoothDevice dispositivo = (BluetoothDevice) mAdapter.getItem(posicionListBluethoot);
                    String direccionBluethoot = dispositivo.getAddress();
                    ((AlmacenGlobal) getApplication()).setDirBluetooth(direccionBluethoot);
                    SharedPreferences settings = getSharedPreferences("preferenciasFile", 0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("addr", direccionBluethoot);
                    editor.commit();

                    Intent i = new Intent(DeviceListActivity.this, ComunicationActivity.class);
                    i.putExtra("Direccion_Bluethoot", direccionBluethoot);

                    startActivity(i);

                }
                else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
                    ((AlmacenGlobal) getApplication()).setDirBluetooth(null);
                }

                mAdapter.notifyDataSetChanged();
            }
        }
    };
}


