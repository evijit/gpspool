package sample;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Queue;
import java.util.*;

import sample.Utility;
import sintulabs.p2p.Ayanda;


public class Coordinator extends Thread{
    HashMap<String, BluetoothDevice> devices;
    Ayanda a;
    String transfer_mess;
    Queue<Integer> list_order;
    Queue<Integer> list_n_signal;

    List<Integer> device;

    Integer n_signal_per_round = 20;

    boolean is_task_done = true;

    private Context context;

    public List<Integer> query_battery(){
        List<Integer> l = new ArrayList<Integer>();
        l.add(100);
        l.add(80);
        l.add(70);
        return l;
    }

    public void transfer_control(){
        //transfer control to the next device with the largest battery level
    }

    public void schedule(){
        List<Integer> batteries = query_battery();
        Integer total_b = 0;
        Integer n_device =  batteries.size();
        for(int i = 0; i < n_device; i++){
            total_b += batteries.get(i);
        }

        for(int i_1 = 0; i_1 < n_device; i_1++){
            Integer idx_max = 0;
            for(int i_2 = 0; i_2 < batteries.size(); i_2++){        //get max battery of entries in the list
                if(batteries.get(i_2) >  batteries.get(idx_max)){
                    idx_max = i_2;
                }
            }

            list_order.add(idx_max);
            list_n_signal.add(n_signal_per_round* batteries.get(idx_max)/total_b);
            batteries.set(idx_max,-1);
        }
    }

    public void send_task(){
        if(this.list_order.isEmpty() == true){
            schedule();
        }
        Integer device_id = list_order.remove();
        Integer n_signal = list_n_signal.remove();

        String notification = "Task- device: "+ String.valueOf(device_id) +" n_signal: "+String.valueOf(n_signal);

        //Toast.makeText(this.context,notification , Toast.LENGTH_LONG).show();
        Log.w("Coordinator", notification);
    }

    public Coordinator(HashMap<String, BluetoothDevice> devices, String transfer_mess, Ayanda a){
        this.devices = devices;
        this.transfer_mess = transfer_mess;
        this.a = a;
        this.list_order = new LinkedList<Integer>();
        this.list_n_signal = new LinkedList<Integer>();
    }

    BluetoothDevice getDeviceByName(String name){
        for (Map.Entry mapElement : devices.entrySet()) {
            String key = (String)mapElement.getKey();
            if (key.contains(name)) {
                return (BluetoothDevice)mapElement.getValue();
            }
        }
        return null;
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
    }

    @Override
    public void run() {
        //Toast.makeText(this.context,"RUN !!!" , Toast.LENGTH_LONG).show();
        selfElect();
        Utility.sleep(5000);
        for(int i = 0; i < 5; ++i){
            castMess("GPS-0.0");
            Utility.sleep(1000);
        }
        castMess("Transfer-Done");
    }
}
