package com.example.farmapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.farmapp.adapters.DeviceAdapter;
import com.example.farmapp.utils.CommonUtils;
import com.example.farmapp.vo.Device;
import com.example.farmapp.utils.DividerItemDecoration;

import java.util.ArrayList;
import java.util.Set;

public class DeviceList extends AppCompatActivity implements DeviceAdapter.Callback{

    // Debugging for LOGCAT
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;


    // declare button for launching website and textview for connection status
    Button tlbutton;
    TextView textView1;

    // EXTRA string to send on to mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Member fields
    private BluetoothAdapter mBtAdapter;
//    private ArrayAdapter mPairedDevicesArrayAdapter;

    RecyclerView mRecyclerView;
    DeviceAdapter mDeviceAdapter;

    LinearLayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        mRecyclerView = findViewById(R.id.mRecyclerView);
        setUp();
    }

    private void setUp() {
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        Drawable dividerDrawable = ContextCompat.getDrawable(this, R.drawable.divider_drawable);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));
        mDeviceAdapter = new DeviceAdapter(new ArrayList<Device>());
        mDeviceAdapter.setCallback(this);
    }

    private void loadDevice(){

        checkBTState();
        CommonUtils.showLoading(DeviceList.this);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ArrayList<Device> mdevices = new ArrayList<>();
                CommonUtils.hideLoading();
                // Get the local Bluetooth adapter
                mBtAdapter = BluetoothAdapter.getDefaultAdapter();

                // Get a set of currently paired devices and append to 'pairedDevices'
                Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

                // Add previosuly paired devices to the array
                if (pairedDevices.size() > 0) {
                    findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);//make title viewable
                    for (BluetoothDevice device : pairedDevices) {
                        mdevices.add(new Device(device.getName(),device.getAddress()));
                    }
                }
                mDeviceAdapter.addItems(mdevices);
                mRecyclerView.setAdapter(mDeviceAdapter);
            }
        }, 2000);

    }

    @Override
    public void onResume()
    {
        super.onResume();
        loadDevice();
    }

    // Set up on-click listener for the list (nicked this - unsure)
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3) {

            textView1.setText("Conectando...");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Make an intent to start next activity while taking an extra which is the MAC address.
            Intent i = new Intent(DeviceList.this, InfoActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter=BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth Activado...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

    @Override
    public void onEmptyViewRetryClick() {
        loadDevice();
    }
}