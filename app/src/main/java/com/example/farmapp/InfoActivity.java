package com.example.farmapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class InfoActivity extends AppCompatActivity {

    TextView txtHunedad,txtTemperatura, txtRecomendaciones,txtBatery,txtRecomendsTemp;
    Handler bluetoothIn;

    final int handlerState = 0;             //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private InfoActivity.ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info);

        txtHunedad = (TextView) findViewById(R.id.txt_humedity);
        txtTemperatura = (TextView) findViewById(R.id.txt_temp);
        txtRecomendaciones = (TextView) findViewById(R.id.txtRecomendaciones);
        txtBatery = findViewById(R.id.txtBatery);
        txtRecomendsTemp = findViewById(R.id.txtRecomendsTemp);

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);              //keep appending to string until ~
                    String[] parts = recDataString.toString().split("x");
                    if(parts.length>=3) {
                        txtHunedad.setText(parts[0] + "%");
                        txtTemperatura.setText(parts[1] + "°");
                        txtBatery.setText(parts[2] + "%");//BATERY
                        if (!parts[0].trim().equals("")) {
                            if (Integer.valueOf(parts[0].trim()) < 75) {
                                txtRecomendaciones.setText("La humedad del cultivo es muy baja al porcentaje ideal, por ello se recomienda activar el sistema de riego ");
                            } else if (Integer.valueOf(parts[0].trim()) > 85) {
                                txtRecomendaciones.setText("La humedad del cultivo es muy alta al porcentaje ideal, por ello se recomienda apagar el sistema de riego ");
                            } else {
                                txtRecomendaciones.setText("La humedad del cultivo es la recomendada, mantener este valor");
                            }
                        }

                        //recomendaciones temperatura
                        if (!parts[0].trim().equals("")) {
                            if (Integer.valueOf(parts[1].trim()) < 5) {
                                txtRecomendsTemp.setText("La temperatura es muy baja, se recomienda realizar plan de contiengencia para proteger el cultivo ");
                            } else if (Integer.valueOf(parts[1].trim()) > 18) {
                                txtRecomendsTemp.setText("La temperatura del cultivo es muy alta, No es optimo activar el riego, puede afectar el cultivo ");
                            } else {
                                txtRecomendsTemp.setText("El clima se encuentra en condiciones optimas, si la humedad <75% puede activar el riego");
                            }
                        }
                    }
                }
                recDataString.delete(0, recDataString.length());      //clear all string data
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceList.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                e.printStackTrace();
                btSocket.close();
            } catch (IOException e2)
            {
                e2.printStackTrace();
                //insert code to deal with this
            }
        }
        mConnectedThread = new InfoActivity.ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    sleep(1000);
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }catch(Exception ex){
                    ex.printStackTrace();
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
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

}
