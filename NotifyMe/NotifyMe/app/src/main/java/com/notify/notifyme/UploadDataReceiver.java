package com.notify.notifyme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class UploadDataReceiver extends BroadcastReceiver {
    Context c;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        c=context;

        //create a hashmap from the database
        MyDBHelper myDB = new MyDBHelper(c);

        Log.i("Alarm fired", "time: "+System.currentTimeMillis());
        if(isNetworkAvailable(context))
        {
            Log.i("Alarm event", "Network Available");
            String dbpath = myDB.exportDatabase();
            UploadToDBTask task = new UploadToDBTask(c, this);
            task.execute(dbpath);
        }
        else
        {
            Log.i("Alarm event", "Network Not Available");
        }
    }

    private boolean isNetworkAvailable(Context c) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() && activeNetworkInfo.getType()==ConnectivityManager.TYPE_WIFI;
    }

    public void onUploadCompleted(Boolean result) {
        if(result) {
            Log.i("Alarm event", "About to truncate");
            // TODO Auto-generated method stub
            MyDBHelper db = new MyDBHelper(c);
            db.clearDB();
            Log.i("Alarm event", "truncate done");
        }
    }

}
