package com.notify.notifyme;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

import java.util.ArrayList;

class CapturedNotification {

    public boolean led = false;
    public int nid=-99;
    public long timePosted=0;
    public String packageName="";
    public int ringerMode=-99;
    public boolean has_default_lights=false;
    public boolean has_default_sound=false;
    public boolean has_default_vibe=false;
    public boolean had_sound=false;
    public String vibration="";
    public int priority=-99;
    public boolean screen_interactive=false;
    public boolean device_idle=false;
    public int screen_mode=-99;
    public int flags = 0;
    public int lock_screen_notifs = -1;
    public String activities = "";
    public double speed;
    public double lat;
    public double lon;
    private boolean found = false;

    public final String strSeparator = ",";

    CapturedNotification (StatusBarNotification sn, int ringmode, boolean interactive, boolean idle, int scrmode, int lock_mode, ArrayList<Boolean> detectedActivities, double speed, double lat, double lon)
    {
        if (detectedActivities.size() > 0) {
            for (int i = 0; i < detectedActivities.size(); i++) {
                if (detectedActivities.get(i)) {
                    activities += String.valueOf(i) + " ";
                    found = true;
                }
            }
        }

        if (!found) {
            activities = String.valueOf(5);
        }

        Notification n = sn.getNotification();

        if (n.ledOnMS>0)
            led=true;

        nid = sn.getId();
        timePosted = sn.getPostTime();
        packageName = sn.getPackageName();
        ringerMode = ringmode;
        lock_screen_notifs = lock_mode;
        this.speed = speed;
        this.lat = lat;
        this.lon = lon;

        if ((n.defaults | Notification.DEFAULT_LIGHTS) == n.defaults)
        {
            has_default_lights=true;
        }
        if ((n.defaults | Notification.DEFAULT_SOUND) == n.defaults)
        {
            has_default_sound = true;
        }
        if ((n.defaults | Notification.DEFAULT_VIBRATE) == n.defaults)
        {
            has_default_vibe=true;
        }
        if(n.sound!=null)
            had_sound = true;
        if(n.vibrate!=null)
        {
            vibration = convertLongArrayToString(n.vibrate);
        }

        priority = n.priority;

        screen_interactive = interactive;
        device_idle = idle;
        screen_mode = scrmode;
        flags = n.flags;
    }

    public String convertLongArrayToString(long[] pattern){
        String str = "";
        for (int i = 0;i<pattern.length; i++) {
            str+=pattern[i];
            // Do not append comma at the end of last element
            if(i<pattern.length-1){
                str = str+strSeparator;
            }
        }
        return str;
    }

}
