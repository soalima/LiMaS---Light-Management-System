package com.proyecto.limas11.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Stack;
import java.util.UUID;

public class BluetoothConnectionService {

    private static final String TAG = "BluetoothConnection";

    private static final String appName = "ChangoSmart";

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static Stack<byte[]> messagesQueue;

    private final BluetoothAdapter myBluetoothAdapter;

    private Context myContext;

    private AcceptThread myInsecureAcceptThread;

    private ConnectThread myConnectThread;

    private ConnectedThread myConnectedThread;

    private BluetoothDevice myDevice;

    private UUID deviceUUID;
    private static  final long PAUSA = 600;

    public BluetoothConnectionService(Context context) {
        myContext = context;
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        messagesQueue = new Stack<>();
        start();
    }

    public UUID getDeviceUUID() {
        return MY_UUID_INSECURE;
    }

    public class AcceptThread extends Thread {
        private final BluetoothServerSocket myServerSocket;

        private AcceptThread() {
            BluetoothServerSocket tmp = null;

            try {
                tmp = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Estableciendo conexión con el servidor con: " + MY_UUID_INSECURE);
            } catch (Exception ex) {
                Log.e(TAG, "AcceptThread: IOException: " + ex.getMessage());
            }

            myServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "Run: AcceptThread Ejecutando...");

            BluetoothSocket socket = null;

            try {
                Log.d(TAG, "Run: RFCOM empezando conexión del servidor...");

                socket = myServerSocket.accept();

                Log.d(TAG, "Run: RFCOM conexión del servidor aceptada.");
            } catch (Exception ex) {
                Log.e(TAG, "AcceptThread: IOException: " + ex.getMessage());
            }

            if (socket != null) {
                connected(socket, myDevice);
            }

            Log.i(TAG, "FIN myAcceptThread");
        }

        public void cancel() {
            Log.d(TAG, "Cancel: Cancelando AcceptThread.");
            try {
                myServerSocket.close();
            } catch (Exception ex) {
                Log.e(TAG, "Cancel: Cierre de AcceptThread, falló ServerSocket. " + ex.getMessage());
            }
        }
    }

    public class ConnectThread extends Thread {
        private BluetoothSocket mySocket;

        private ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: comenzado.");
            myDevice = device;
            deviceUUID = uuid;
            BluetoothSocket tmp = null;

            try {
                Log.d(TAG, "ConnectThread: Tratando de crear  InsecureRfcommSocket usando UUID: " + MY_UUID_INSECURE);
                tmp = myDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (Exception ex) {
                Log.e(TAG, "ConnectThread: No se pudo crear un InsecureRfcommSocket " + ex.getMessage());
            }

            mySocket = tmp;
        }

        public void run() {
            Log.i(TAG, "EJECUTANDO myConnectThread.");

            myBluetoothAdapter.cancelDiscovery();

            try {
                mySocket.connect();

                Log.d(TAG, "ConnectThread: conectado!.");
            } catch (Exception ex) {

                try {
                    mySocket.close();
                    Log.d(TAG, "Run: Socket cerrado.");
                } catch (Exception e1) {
                    Log.e(TAG, "myConnectThread: Run: No es posible cerrar la conexión de el socket " + e1.getMessage());
                }
                Log.d(TAG, "Run: ConnectThread: No es posible conectarse al UUID: " + MY_UUID_INSECURE);
            }

            connected(mySocket, myDevice);
        }

        private void cancel() {
            try {
                Log.d(TAG, "Cancel: Cerrndo socket cliente.");
                mySocket.close();
            } catch (Exception ex) {
                Log.e(TAG, "Cancel: close() de mySocket en ConnectThread falló. " + ex.getMessage());
            }
        }
    }

    private synchronized void start() {
        Log.d(TAG, "start");

        if (myConnectThread != null) {
            myConnectThread.cancel();
            myConnectThread = null;
        }
        if (myInsecureAcceptThread == null) {
            myInsecureAcceptThread = new AcceptThread();
            myInsecureAcceptThread.start();
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "StartClient: Iniciado.");


        myConnectThread = new ConnectThread(device, uuid);
        myConnectThread.start();
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket myLocalSocket;
        private final InputStream myInStream;
        private final OutputStream myOutStream;

        private ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Iniciado.");

            myLocalSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = myLocalSocket.getInputStream();
                tmpOut = myLocalSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            myInStream = tmpIn;
            myOutStream = tmpOut;
        }

        public void run() {

            byte[] buffer = new byte[1024];

            int bytes = 0;

            while (true) {
                while (!messagesQueue.empty()) {
                    myConnectedThread.write(messagesQueue.pop());
                }
                try {
                    while (myInStream.available() > 0) {
                        bytes = myInStream.read(buffer);
                        if (bytes < 0) break;
                        Log.d(TAG, "InputStream: " + "No hay mas mensajes - " + bytes);
                    }

                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);
                    Intent incomingMessageIntent = new Intent("IncomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingMessageIntent);
                    try {
                        Thread.sleep(PAUSA);
                    } catch (Exception e) {
                        Log.e(TAG, "READ: Error al recibir el mensaje. " + e.getMessage());
                    }

                } catch (Exception e) {
                    Log.e(TAG, "READ: Error al recibir el mensaje. " + e.getMessage());
                    //Es importante cortar con el break el ciclo.
                    break;
                }
            }
        }
        private void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Escribiendo mensaje obtenido: " + text);
            try {
                myOutStream.write(bytes);
            } catch (Exception e) {
                Log.e(TAG, "write: Error escribiendo mensaje obtenido " + e.getMessage());
            }
        }
        public void cancel() {
            try {
                myLocalSocket.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }
    private void connected(BluetoothSocket mySocket, BluetoothDevice myDevice) {
        Log.d(TAG, "connected: Starting.");
        myConnectedThread = new ConnectedThread(mySocket);
        myConnectedThread.start();
    }
    public void cancel() {
        Log.d(TAG, "cancel: Canceling socket.");

        myConnectedThread.cancel();
    }
    public void write(byte[] out) {
        Log.d(TAG, "write: Write llamado.");
        messagesQueue.push(out);
    }
}