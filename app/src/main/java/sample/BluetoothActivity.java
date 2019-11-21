package sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Iterator;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sintulabs.ayanda.R;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.Bluetooth;
import sintulabs.p2p.IBluetooth;

/**
 * Created by sabzo on 1/14/18.
 */

public class BluetoothActivity extends AppCompatActivity {
    private Button btnAnnounce;
    private Button btnDiscover;
    private Bluetooth bt;
    private ListView lvBtDeviceNames;
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();

    Ayanda a;

    HashSet<String> connecteddevices = new HashSet<>();

    private Coordinator c;

    public void sendtoall(View view) {

        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String)mapElement.getKey();
            if (key.contains("GPS")) {

//                Toast.makeText(BluetoothActivity.this, "Inside sendall, sending to: " + key, Toast.LENGTH_LONG)
//                        .show();
                Log.w("Debug", "Inside sendall, sending to: " + key);

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();

                String message = "Hello from " + getLocalBluetoothName() + " to " + key + " battery: " + getBattery_percentage();
                try {
                    a.btSendData(device, message.getBytes()); // maybe a class for a device that's connected
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        a = new Ayanda(this, new IBluetooth() {
            @Override
            public void actionDiscoveryStarted(Intent intent) {

            }

            @Override
            public void actionDiscoveryFinished(Intent intent) {

            }

            @Override
            public void stateChanged(Intent intent) {

            }

            @Override
            public void scanModeChange(Intent intent) {

            }

            @Override
            public void actionFound(Intent intent) {
                peersAdapter.clear();
                Set<String> tt = a.btGetDeviceNamesDiscovered();
                removenongps(tt);
                peersAdapter.addAll(tt);
                devices = a.btGetDevices();
            }

            @Override
            public void dataRead(byte[] bytes, int length) {
                // This is the listening method
                String readMessage = new String(bytes, 0, length);
                Toast.makeText(BluetoothActivity.this, readMessage, Toast.LENGTH_LONG)
                        .show();

                String dname = readMessage.substring(readMessage.length() - 9);
                connecteddevices.add(dname);

                Log.w("Debug",readMessage);
            }

            @Override
            public void connected(BluetoothDevice device) {
                String message = "Connection established with "+ getLocalBluetoothName() ;
                try {
                    a.btSendData(device, message.getBytes()); // maybe a class for a device that's connected
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, null, null);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                Bluetooth.BT_PERMISSION_REQUEST_LOCATION);
        setContentView(R.layout.bluetooth_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        createView();
        setListeners();

//        c = new Coordinator(BluetoothActivity.this);
//        c.run();


//        sendToAll();
        a.btAnnounce();
    }


    String getBattery_percentage()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct * 100;

        return String.valueOf(Math.round(p));
    }

    // Method to remove elements from a set in java
    public static void removenongps(Set<String> ints)
    {

        Iterator<String> it = ints.iterator();

        while (it.hasNext()) {
            if (!it.next().contains("GPS")) {	// remove even elements
                it.remove();
            }
        }
    }

    public String getLocalBluetoothName(){
        BluetoothAdapter mBluetoothAdapter = null;
        if(mBluetoothAdapter == null){
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }
        String name = mBluetoothAdapter.getName();
        if(name == null){
            System.out.println("Name is null!");
            name = mBluetoothAdapter.getAddress();
        }
        return name;
    }


    private void createView() {
        lvBtDeviceNames = (ListView) findViewById(R.id.lvBtDeviceNames);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvBtDeviceNames.setAdapter(peersAdapter);
        peersAdapter.notifyDataSetChanged();
        btnAnnounce = (Button) findViewById(R.id.btnBtAnnounce);
        btnDiscover = (Button) findViewById(R.id.btnBtDiscover);
//        a.btAnnounce();
//        a.btDiscover();

    }

    private void setListeners() {
        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.btnBtAnnounce:
                        a.btAnnounce();
                        break;
                    case R.id.btnBtDiscover:
                        a.btDiscover();
                        break;
                }
            }
        };
        AdapterView.OnItemClickListener clickPhone = new AdapterView.OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                BluetoothDevice device = devices.get(peerNames.get(pos));
                a.btConnect(device);
            }
        };

        btnAnnounce.setOnClickListener(btnClick);
        btnDiscover.setOnClickListener(btnClick);
        lvBtDeviceNames.setOnItemClickListener(clickPhone);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//        switch (id) {
//            case R.id.miLan:
//                startActivity(new Intent(this, LanActivity.class ));
//                finish();
//                break;
//            case R.id.miWd:
//                startActivity(new Intent(this, WifiDirectActivity.class ));
//                finish();
//                break;
//        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        a.btRegisterReceivers();
    }



    @Override
    protected void onPause() {
        super.onPause();
        a.btUnregisterReceivers();
    }


    public void start(View view) {
        a.btDiscoverandannounce();

        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String) mapElement.getKey();
            if (key.contains("GPS")) {

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();
                a.btConnect(device); // maybe a class for a device that's connected
            }
        }




    }


}
