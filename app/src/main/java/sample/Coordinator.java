package sample;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Queue;
import java.util.*;

import sample.Utility;


public class Coordinator extends Thread{
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

    public Coordinator(Context context){
        this.context = context;
        this.list_order = new LinkedList<Integer>();
        this.list_n_signal = new LinkedList<Integer>();


    }

    @Override
    public void run() {
        //Toast.makeText(this.context,"RUN !!!" , Toast.LENGTH_LONG).show();
        Log.w("Coordinator", "RUN !!!");
        while(true) {
            if(is_task_done){
                send_task();
                is_task_done = false;

                Utility.sleep(5000);

                is_task_done = true;

            }
        }
    }
}
