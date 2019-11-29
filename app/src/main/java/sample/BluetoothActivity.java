package sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sintulabs.ayanda.R;
import sintulabs.p2p.Ayanda;
import sintulabs.p2p.Bluetooth;
import sintulabs.p2p.IBluetooth;

import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by sabzo on 1/14/18.
 */

public class BluetoothActivity extends AppCompatActivity implements OnMapReadyCallback{
    private Button btnAnnounce;
    private Button btnDiscover;
    private Bluetooth bt;
    private ListView lvBtDeviceNames;
    private ArrayAdapter<String> peersAdapter = null;
    private List peerNames = new ArrayList();
    private HashMap<String, BluetoothDevice> devices = new HashMap<>();

    HashMap<String, Float> battery_level;

    String transfer_mess = "";

    Integer clock = -1;

    //String behavior = "Transfer~GPSPool_0~GPSPool_1~GPSPool_3";

    boolean is_leader = false;
    boolean is_timeout = false;

    Integer sleep_transfer = 15000;
    Integer sleep_GPS = 8000;

    Integer n_total = 0;

    Float round = 0f;
    Float n_signal_round = 10000f;
    Ayanda a;

    double lat = 0;
    double lon = 0;

    HashSet<String> connecteddevices = new HashSet<>();

    private Coordinator c;
    private GoogleMap mMap;

    float zoom_level = 14f;
    BluetoothDevice getDeviceByName(String name) {
        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String) mapElement.getKey();
            if (key.contains(name)) {
                return (BluetoothDevice) mapElement.getValue();
            }
        }
        return null;
    }

    public List<String> parse_transfer_mess() {
        String[] tokens = this.transfer_mess.split("~");
        List<String> ret = new LinkedList<>();
        if (tokens[1].equals("Done")) {
            return null;
        } else {
            for (int i = 1; i < tokens.length; ++i) {
                ret.add(tokens[i]);
            }
        }
        return ret;
    }

    public Integer compute_n_signal(){
        Float n_total_battery = 0.0f;
        for (Map.Entry<String, Float> entry : this.battery_level.entrySet()) {
            n_total_battery += entry.getValue();
        }
        Float battery = Float.valueOf(getBattery_percentage());
        n_total_battery += battery;
        return Math.round(n_signal_round/n_total_battery*battery);
    }

    public void sendtoall(View view) {

        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String) mapElement.getKey();
            if (key.contains("GPS")) {

//                Toast.makeText(BluetoothActivity.this, "Inside sendall, sending to: " + key, Toast.LENGTH_LONG)
//                        .show();
                Log.w("Debug", "Inside sendall, sending to: " + key);

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();

                String message = "Leader~" + getLocalBluetoothName();
                try {
                    a.btSendData(device, message.getBytes()); // maybe a class for a device that's connected
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void updateUI() {
        this.peersAdapter.clear();
        Set<String> tt = a.btGetDeviceNamesDiscovered();
        removenongps(tt);
        this.peersAdapter.addAll(tt);
    }

    public String create_transfer_mess() {
        List<String> ret = parse_transfer_mess();
        String mess = "Transfer";
        if(ret.size() == 1){
            for (Map.Entry<String, Float> entry : this.battery_level.entrySet()) {
                mess += "~"+entry.getKey();
            }
        }else{
            for(int i =1; i < ret.size();++i) {
                mess += "~"+ret.get(i);
            }
        }
        return mess;
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
                BluetoothActivity.this.updateUI();
                devices = a.btGetDevices();
            }

            @Override
            public void dataRead(byte[] bytes, int length) {
                // This is the listening method
                String mess = new String(bytes, 0, length);

                Toast.makeText(BluetoothActivity.this, mess, Toast.LENGTH_LONG)
                        .show();

                TextView tv_log = (TextView) findViewById(R.id.logt);
                tv_log.setMovementMethod(new ScrollingMovementMethod());

                String om = tv_log.getText().toString();;
                om = om + '\n'+ mess;
                tv_log.setText(om);


                //String dname = readMessage.substring(readMessage.length() - 9);
                //connecteddevices.add(dname);

                String[] tokens = mess.split("~");
                String mess_type = tokens[0];

                /*
                if(is_timeout==false){
                    Toast.makeText(BluetoothActivity.this, "Initialize time out!", Toast.LENGTH_LONG)
                            .show();
                    new Thread() {          //this would make sure it will not block the dataRead thread
                        public void run() {
                            while(true) {
                                Integer old_clock = BluetoothActivity.this.clock;
                                Utility.sleep(sleep_transfer*2);
                                if ((BluetoothActivity.this.clock <= old_clock) & (old_clock > 0)) {

                                    BluetoothActivity.this.transfer_mess = BluetoothActivity.this.create_transfer_mess();

                                    castMess(BluetoothActivity.this.transfer_mess);

                                }
                                Log.w("Debug", "progress");
                            }
                        }
                    }.start();
                    is_timeout = true;
                }

                
                 */
                clock += 1;

                if(mess_type.equals("Leader")){
                    round += 1;
                    a.btDiscoverandannounce();

                    String leader = tokens[1];
                    BluetoothDevice device = getDeviceByName(leader);

                    String ack_mess = "Ack~" + getLocalBluetoothName() + "~" + getBattery_percentage();

                    try {
                        a.btSendData(device, ack_mess.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(BluetoothActivity.this, "New leader :" + leader, Toast.LENGTH_LONG)
                            .show();
                } else if (mess_type.equals("Ack")) {
                    String device_id = tokens[1];
                    Float battery = Float.valueOf(tokens[2]);
                    battery_level.put(device_id, battery);
                    if (is_leader == false) {
                        is_leader = true;
                        new Thread() {          //this would make sure it will not block the dataRead thread
                            public void run() {
                                Utility.sleep(sleep_transfer);
                                Integer n_signal = BluetoothActivity.this.compute_n_signal();
                                if(n_signal == 0){
                                    n_signal = 1;
                                }
                                //castMess("Leader~" + getLocalBluetoothName());



                                LocationManager manager = (LocationManager) BluetoothActivity.this.getSystemService(Context.LOCATION_SERVICE);
                                if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                        ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                Location loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);



                                //Toast.makeText( getApplicationContext(),"My current location is: " + "Latitud =" +
                                //        loc.getLatitude() + "Longitud = " + loc.getLongitude(),Toast.LENGTH_SHORT).show();
                                try {
                                    lat = loc.getLatitude();
                                    lon = loc.getLongitude();

                                }catch (Exception e){
                                }


                                for (int i = 0; i < n_signal; ++i) {
                                    n_total+=1;
                                    castMess("GPS~" + lat + "=" + lon + "~n_message:" + i + "~n_round:" + round);
                                    Utility.sleep(sleep_GPS);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                        mMap.clear();
                                        LatLng mkr = new LatLng(lat, lon);
                                        mMap.addMarker(new MarkerOptions().position(mkr).title("Received Location"));
                                        mMap.moveCamera(CameraUpdateFactory.newLatLng(mkr));
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mkr, zoom_level));

                                        TextView tv_bat = (TextView) findViewById(R.id.txbat);
                                        TextView tv_txcount = (TextView) findViewById(R.id.txcount);

                                        tv_bat.setText(getBattery_percentage_MAA());
                                        tv_txcount.setText(String.valueOf(n_total));
                                        }
                                    });
                                }

//                                if (manager != null) {
//
//                                    manager.removeUpdates((LocationListener) BluetoothActivity.this);
//
//                                }

                                BluetoothActivity.this.transfer_mess = BluetoothActivity.this.create_transfer_mess();

                                castMess(BluetoothActivity.this.transfer_mess);

                            }
                        }.start();
                    }
                } else if (mess_type.equals("Transfer")) {
                    a.btDiscoverandannounce();

                    BluetoothActivity.this.transfer_mess = mess;
                    Utility.sleep(1000);
                    is_leader = false;
                    List<String> ret = parse_transfer_mess();
                    if (ret.get(0).equals(getLocalBluetoothName())) {
                        selfElect();
                    }

                }
                else if (mess_type.equals(("GPS"))){
                    n_total+=1;

                    String[] toks = mess.split("~");
                    String[] ll = toks[1].split("=");
                    Double lt = Double.valueOf(ll[0]);
                    Double ln = Double.valueOf(ll[1]);
                    mMap.clear();
                    LatLng mkr = new LatLng(lt, ln);
                    mMap.addMarker(new MarkerOptions().position(mkr).title("Received Location"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(mkr));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mkr,zoom_level));

                    TextView tv_bat = (TextView) findViewById(R.id.txbat);
                    TextView tv_txcount = (TextView) findViewById(R.id.txcount);

                    tv_bat.setText(getBattery_percentage_MAA());
                    tv_txcount.setText(String.valueOf(n_total));
                }
                Log.w("Debug", mess_type);
            }

            @Override
            public void connected(BluetoothDevice device) {
                String message = "Leader~" + getLocalBluetoothName();//"Hello";//
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        battery_level = new HashMap<>();
        createView();
        setListeners();
        //a.btAnnounce();
        a.btDiscoverandannounce();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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

    String getBattery_percentage_MAA()
    {
        Object mPowerProfile_ = null;
        double batteryCapacity = 0.0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {
            e.printStackTrace();
        }

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct *(float)batteryCapacity;

        return String.valueOf(p);
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
        lvBtDeviceNames = findViewById(R.id.lvBtDeviceNames);
        peersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, peerNames);
        lvBtDeviceNames.setAdapter(peersAdapter);
        peersAdapter.notifyDataSetChanged();
        btnAnnounce = findViewById(R.id.btnBtAnnounce);
        btnDiscover = findViewById(R.id.btnBtDiscover);
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

    public void castMess(String mess) {

        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String)mapElement.getKey();
            if (key.contains("GPS")) {

//                Toast.makeText(BluetoothActivity.this, "Inside sendall, sending to: " + key, Toast.LENGTH_LONG)
//                        .show();
                Log.w("Debug", "Inside sendall, sending to: " + key);

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();

                try {
                    a.btSendData(device, mess.getBytes()); // maybe a class for a device that's connected
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void selfElect() {
        a.btDiscoverandannounce();



        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String) mapElement.getKey();
            if (key.contains("GPS")) {

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();
                a.btConnect(device); // maybe a class for a device that's connected
            }
        }
        //castMess("Leader~"+getLocalBluetoothName());
    }

    public void start(View view) {
        /*
        a.btDiscoverandannounce();

        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String) mapElement.getKey();
            if (key.contains("GPS")) {

                BluetoothDevice device = (BluetoothDevice) mapElement.getValue();
                a.btConnect(device); // maybe a class for a device that's connected
            }
        }
         */
        //a.btDiscoverandannounce();
        //c = new Coordinator(this.devices,"Transfer~Done",a);
        //c.start();

        TextView tv_log = (TextView) findViewById(R.id.logt);
        tv_log.setMovementMethod(new ScrollingMovementMethod());

        tv_log.setText("");

        selfElect();
        transfer_mess = "Transfer~"+getLocalBluetoothName();//behavior;//"Transfer~GPSPool_0~GPSPool_4";
        Log.w("Debug","exit start");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Starting marker"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }


    public void constgps(View view) {
        TextView tv_log = (TextView) findViewById(R.id.logt);
        tv_log.setMovementMethod(new ScrollingMovementMethod());

        tv_log.setText("");

        new Thread() {          //this would make sure it will not block the dataRead thread
            public void run() {
                while (true) {

                    LocationManager manager = (LocationManager) BluetoothActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(BluetoothActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    Location loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                    n_total += 1;
                    //Toast.makeText( getApplicationContext(),"My current location is: " + "Latitud =" +
                    //        loc.getLatitude() + "Longitud = " + loc.getLongitude(),Toast.LENGTH_SHORT).show();
                    try {
                        lat = loc.getLatitude();
                        lon = loc.getLongitude();

                    } catch (Exception e) {
                    }


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            TextView tv_bat = (TextView) findViewById(R.id.txbat);
                            TextView tv_txcount = (TextView) findViewById(R.id.txcount);

                            tv_bat.setText(getBattery_percentage_MAA());
                            tv_txcount.setText(String.valueOf(n_total));

                            TextView tv_log = (TextView) findViewById(R.id.logt);
                            tv_log.setMovementMethod(new ScrollingMovementMethod());

                            String om = tv_log.getText().toString();;
                            om = om + "\n GPS" ;
                            tv_log.setText(om);

                            mMap.clear();
                            LatLng mkr = new LatLng(lat, lon);
                            mMap.addMarker(new MarkerOptions().position(mkr).title("Received Location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(mkr));
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mkr, zoom_level));
                        }
                    });

                    Utility.sleep(sleep_GPS);
                }
            }
        }.start();
    }
}
