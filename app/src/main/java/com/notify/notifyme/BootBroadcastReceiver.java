package com.notify.notifyme;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            Intent serviceIntent = new Intent(context, MyNotificService.class);
            context.startService(serviceIntent);
            return;
        }
        /*
        if(intent.getAction().equalsIgnoreCase(Intent.ACTION_PACKAGE_CHANGED)){
            Log.e("Packagechanged receiver", "Service stopped");
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(new ComponentName(context, MyNotificService.class),
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        }
        */
    }
}

