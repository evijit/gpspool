package sample;

import android.util.Log;

import sample.Utility;

public class Worker extends Thread{
    Integer n_signal = 0;



    public Worker(){

    }

    public void get_GPS(){
        Log.w("Worker", "get GPS !!!");
    }

    public void cast_GPS(){
        Log.w("Worker", "cast GPS !!!");
    }

    public void confirm_task_completion(){

    }



    @Override
    public void run() {
        //Toast.makeText(this.context,"RUN !!!" , Toast.LENGTH_LONG).show();
        Log.w("Worker", "RUN !!!");
        while(true) {
            while(n_signal > 0) {
                Utility.sleep(5000);
                cast_GPS();
            }
            Utility.sleep(5000);
        }
    }
}
