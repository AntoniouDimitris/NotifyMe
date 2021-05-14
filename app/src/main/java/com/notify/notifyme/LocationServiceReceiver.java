package com.notify.notifyme;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class LocationServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.i("Alarm", "went off");
        if (intent.getAction().equals("ALRMTOSEND")) {
            Log.i("AlarmIntent", "found");
            boolean check = false;
            ActivityManager manager;
            manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                //Log.e("running services", service.service.getClassName());
                if (LocationService.class.getName().equals(service.service.getClassName())) {
                    check = true;
                } else {
                    check = false;
                }
            }

            if (!check) {
                Log.i("Restarted", "service");
                Intent serviceIntent = new Intent(context, LocationService.class);
                ContextCompat.startForegroundService(context, serviceIntent);
            }
        }
    }
}
