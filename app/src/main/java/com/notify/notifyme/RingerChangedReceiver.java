package com.notify.notifyme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class RingerChangedReceiver extends BroadcastReceiver {

    private final String tag = "RingerReceiver";
    private SharedPreferences sp;
    public int MY_NOTIFICATION_ID=220194;

    public long lastinsertid=0;
    private long ringereventid=0;

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.e(tag,intent.getAction());

        MyDBHelper db = new MyDBHelper(context);
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        //insert latest ringer change event
        ringereventid=db.insertRingerChangeData(am.getRingerMode());

        lastinsertid = db.insertQuestionnaireNotifData(MY_NOTIFICATION_ID,System.currentTimeMillis()/1000, ringereventid);

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = sp.edit();
        e.putLong("lastinsertid",lastinsertid);
        e.commit();

        Log.i(tag, "Ringer mode changed "+ am.getRingerMode());

        notifyToAnswerQuestionnaire(context);
        
        Log.i(tag, "Questionnaire's notification is :"+lastinsertid);


    }

    private void notifyToAnswerQuestionnaire(Context con) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        sp = PreferenceManager.getDefaultSharedPreferences(con);
        long lastform = sp.getLong("lastform", -1);

        //if more than 4 hours have passed since the last form time & it's not after 11pm or before 7am
        if (calendar.get(Calendar.HOUR_OF_DAY)<=20 && calendar.get(Calendar.HOUR_OF_DAY)>=18
            /*&& System.currentTimeMillis() - lastform > 4*60*60*1000*/) {

            Log.i("CALENDAR", calendar.get(Calendar.HOUR_OF_DAY)+"");
            //Toast.makeText(con, "Ringer mode changed!", Toast.LENGTH_LONG).show();

            // notify that Ringer mode changed and go to RingerChangedActivity
            Intent myIntent = new Intent(con, RingerChangedActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(con, 0, myIntent, 0);

            NotificationCompat.Builder notification;
            // Build the notification.
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
                notification = new NotificationCompat.Builder(con, "ABCDEF");
                Log.i("builder", "os version "  +  android.os.Build.VERSION.SDK_INT);
            }
            else {
                NotificationManager notificationManager = (NotificationManager) con.getSystemService(NOTIFICATION_SERVICE);
                NotificationChannel channel = notificationManager.getNotificationChannel("ABCDEF");
                if(channel==null) {
                    CharSequence name = "NotifyMeChannel";
                    String description = "NotifyMe experiment";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    channel = new NotificationChannel("ABCDEF", name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    notificationManager.createNotificationChannel(channel);
                }
                notification = new NotificationCompat.Builder(con, "ABCDEF" );
            }
            notification.setAutoCancel(true); // Whenever clicked a notification, it is deleted from the top system status bar.
            notification.setSmallIcon(R.drawable.ic_launcher);
            notification.setTicker("QuietSense");
            //notification.setWhen(System.currentTimeMillis());
            notification.setContentTitle(con.getString(R.string.app_name));
            notification.setContentText(con.getString(R.string.quest_notif_content));
            notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notification.setDefaults(Notification.DEFAULT_SOUND);
            notification.setContentIntent(pendingIntent);

            NotificationManager notificationManager = (NotificationManager) con.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(MY_NOTIFICATION_ID, notification.build());
        }
        else {
            //Toast.makeText(con, "In 4 hours from now you will be notified", Toast.LENGTH_LONG).show();
        }
    }
}
