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

//Clase helper que brinda servicios de comunicación (envío/recepción) de información con otros dispositivos bt vía threads.
public class BluetoothConnectionService {
    //Tag a modo de log
    private static final String TAG = "BluetoothConnection";

    //Emisor de la información hacia otros dispositivos.
    private static final String appName = "ChangoSmart";

    //UUID necesario para la comunicación.
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static Stack<byte[]> messagesQueue;

    private final BluetoothAdapter myBluetoothAdapter;

    private Context myContext;

    //Clases personalizadas que se van a encargar de el proceso de comunicación (threads).
    private AcceptThread myInsecureAcceptThread;

    private ConnectThread myConnectThread;

    private ConnectedThread myConnectedThread;

    private BluetoothDevice myDevice;

    //UUID del dispositivo con el que voy a comunicarme.
    private UUID deviceUUID;
    private static  final long PAUSA = 600;

    //private ProgressDialog myProgressDialog;

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

            //Se crea un nuevo socket para escuchar la comunicación
            try {
                tmp = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID_INSECURE);

                Log.d(TAG, "AcceptThread: Estableciendo conexión con el servidor con: " + MY_UUID_INSECURE);
            } catch (Exception ex) {
                Log.e(TAG, "AcceptThread: IOException: " + ex.getMessage());
            }

            myServerSocket = tmp;
        }

        //Ejecución del socket
        public void run() {
            Log.d(TAG, "Run: AcceptThread Ejecutando...");

            BluetoothSocket socket = null;

            try {
                //Esto es bloqueante y solo retorna conexión exitosa o exception.
                Log.d(TAG, "Run: RFCOM empezando conexión del servidor...");

                //Se asigna el socket designado si la conexión fue exitosa.
                socket = myServerSocket.accept();

                Log.d(TAG, "Run: RFCOM conexión del servidor aceptada.");
            } catch (Exception ex) {
                Log.e(TAG, "AcceptThread: IOException: " + ex.getMessage());
            }

            if (socket != null) {
                //Realizo la conexión.
                connected(socket, myDevice);
            }

            Log.i(TAG, "FIN myAcceptThread");
        }

        //Cierre de conexión del socket.
        public void cancel() {
            Log.d(TAG, "Cancel: Cancelando AcceptThread.");
            try {
                //Cierre de la conexión
                myServerSocket.close();
            } catch (Exception ex) {
                Log.e(TAG, "Cancel: Cierre de AcceptThread, falló ServerSocket. " + ex.getMessage());
            }
        }
    }

    //Clase interna de un socket conectado al servidor.
    public class ConnectThread extends Thread {
        private BluetoothSocket mySocket;

        private ConnectThread(BluetoothDevice device, UUID uuid) {
            Log.d(TAG, "ConnectThread: comenzado.");
            myDevice = device;
            deviceUUID = uuid;
            BluetoothSocket tmp = null;

            //Se obtiene un BluetoothSocket para la conexión con el dispositivo BluetoothDevice.
            try {
                Log.d(TAG, "ConnectThread: Tratando de crear  InsecureRfcommSocket usando UUID: " + MY_UUID_INSECURE);
                tmp = myDevice.createRfcommSocketToServiceRecord(deviceUUID);
            } catch (Exception ex) {
                Log.e(TAG, "ConnectThread: No se pudo crear un InsecureRfcommSocket " + ex.getMessage());
            }

            //Asigno el socket obtenido
            mySocket = tmp;
        }

        public void run() {
            Log.i(TAG, "EJECUTANDO myConnectThread.");

            //Siempre se debe cancelar la búsqueda para ahorrar recursos
            myBluetoothAdapter.cancelDiscovery();

            //Se establece la conexión con el BluetoothSocket
            try {
                mySocket.connect();

                Log.d(TAG, "ConnectThread: conectado!.");
            } catch (Exception ex) {
                //Se trata de cerrar la conexión del socket.
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

        //Cierre del thread ConnectThread
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

        //Se cancela cualquier intento de un thread de establecer una conexión
        if (myConnectThread != null) {
            myConnectThread.cancel();
            myConnectThread = null;
        }
        if (myInsecureAcceptThread == null) {
            myInsecureAcceptThread = new AcceptThread();
            myInsecureAcceptThread.start();
        }
    }

    //Método para iniciar el cliente, prepara el ambiente para enviar / recibir información.
    public void startClient(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "StartClient: Iniciado.");


        myConnectThread = new ConnectThread(device, uuid);
        myConnectThread.start();
    }

    //Clase de un socket ya conectado.
    public class ConnectedThread extends Thread {
        private final BluetoothSocket myLocalSocket;
        private final InputStream myInStream;
        private final OutputStream myOutStream;

        private ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "ConnectedThread: Iniciado.");

            myLocalSocket = socket;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            //Se le asignan los streams correspondientes del socket, tanto para input como para output.
            try {
                tmpIn = myLocalSocket.getInputStream();
                tmpOut = myLocalSocket.getOutputStream();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Si la asignación salió bien, se le asignan a los streams finales.
            myInStream = tmpIn;
            myOutStream = tmpOut;
        }

        //Método que ejecuta el pase de información.
        public void run() {
            //Buffer para almacenar el mensaje
            byte[] buffer = new byte[1024];

            // bytes retornados por el read();
            int bytes = 0;

            // Siempre se sigue escuchando por inputs aunque haya excepciones.
            while (true) {
                //Antes de leer envía lo que tenga almacenado, si hay algo para escribir, en caso de haberlo, escribe.
                while (!messagesQueue.empty()) {
                    myConnectedThread.write(messagesQueue.pop());
                }
                // Se lee del input stream.
                try {
                    //El available se encarga de que el read no detenga la ejecución del hilo si no tiene nada que leer.
                    while (myInStream.available() > 0) {
                        bytes = myInStream.read(buffer);
                        if (bytes < 0) break;
                        Log.d(TAG, "InputStream: " + "No hay mas mensajes - " + bytes);
                    }

                    String incomingMessage = new String(buffer, 0, bytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    //Se declara un intent implicito para que al recibir un mensaje del arduino lo comunique a la vista que esté activa en ese momento.
                    Intent incomingMessageIntent = new Intent("IncomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(myContext).sendBroadcast(incomingMessageIntent);

                    //Se espera una cantidad de 2 segundo para que se llene el buffer en caso de que haya algo.
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

        //Se llama a este método desde el activity para enviar información al dispositivo conectado.
        private void write(byte[] bytes) {
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Escribiendo mensaje obtenido: " + text);
            try {
                myOutStream.write(bytes);
            } catch (Exception e) {
                Log.e(TAG, "write: Error escribiendo mensaje obtenido " + e.getMessage());
            }
        }

        /* Se llama este método desde el activity para finalizar la conexión */
        public void cancel() {
            try {
                myLocalSocket.close();
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    //Método que inicializa la conexión del thread conectado.
    private void connected(BluetoothSocket mySocket, BluetoothDevice myDevice) {
        Log.d(TAG, "connected: Starting.");

        // Se inicializa el thread para gestionar las operaciones de stream.
        myConnectedThread = new ConnectedThread(mySocket);
        myConnectedThread.start();
    }

    //Método que cierra la conexión.
    public void cancel() {
        Log.d(TAG, "cancel: Canceling socket.");

        myConnectedThread.cancel();
    }

    //Método que realiza la escritura de la información.
    public void write(byte[] out) {
        // Se sincronizan las copias de ConnectedThread
        Log.d(TAG, "write: Write llamado.");

        //Se agrega el mensaje recibido a una lista para ser enviado por el thread principal.
        messagesQueue.push(out);
    }
}