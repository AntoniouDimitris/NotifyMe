package com.notify.notifyme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class RingerChangedReceiver extends BroadcastReceiver {

    private final String tag = "RingerReceiver";
    private SharedPreferences sp;
    @Override
    public void onReceive(Context context, Intent intent) {

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        if(sp.getBoolean("consent", false)==false)
            return;

        Log.e(tag,intent.getAction());
        MyDBHelper db = new MyDBHelper(context);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        db.insertRingerChangeData(am.getRingerMode());
        Log.i(tag, "Ringer mode changed"+ am.getRingerMode());
    }
}
