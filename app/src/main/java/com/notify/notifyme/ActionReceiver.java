package com.notify.notifyme;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class ActionReceiver extends BroadcastReceiver {

    MyDBHelper myDB;
    private int choice;
    private long timePosted;
    private int id = 4952;

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String action = intent.getAction();
        myDB = new MyDBHelper(context);

        if(action.equals("action1")) {

            Log.i("driving","Pressed Yes");

            timePosted = sp.getLong("driving", 0);
            choice = 1;
            myDB.updateDriving(id, timePosted, choice);

            AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

            String  s = Context.NOTIFICATION_SERVICE;
            NotificationManager mNM = (NotificationManager) context.getSystemService(s);
            mNM.cancel(id);
        }
        else if (action.equals("action2")) {

            Log.i("driving","Pressed No");

            timePosted = sp.getLong("driving", 0);
            choice = 2;
            myDB.updateDriving(id, timePosted, choice);

            String  s = Context.NOTIFICATION_SERVICE;
            NotificationManager mNM = (NotificationManager) context.getSystemService(s);
            mNM.cancel(id);
        }
    }

}
