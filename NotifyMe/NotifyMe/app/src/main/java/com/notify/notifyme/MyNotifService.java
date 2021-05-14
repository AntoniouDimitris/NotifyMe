package com.notify.notifyme;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MyNotifService extends NotificationListenerService
{
    MyDBHelper myDB;
    private final String TAG = "Notification Service";
    private PlaceDetectionClient mPlaceDetectionClient;
    private Place bestplace;
    private StatusBarNotification sss;
    private RingerChangedReceiver rs;
    private SharedPreferences sp;


    public void onCreate()
    {
        super.onCreate();
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ MyNotifService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //set up receivers

        rs = new RingerChangedReceiver();
        registerReceiver(rs, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

        //set up uploading service
        Intent alarmIntent = new Intent(this, UploadDataReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+60*1000, 60*1000, pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HOUR*6, pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60*1000, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HOUR*6, pendingIntent);

        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 3000, pendingIntent );

        Log.i(TAG, "Alarm set ");
        Log.i(TAG,"OnCreate complete");


    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

        Log.i("Tralalaal", "tralalaeooeoele " +sbn.getPackageName());

        try {
            //check if we are past the collection date
            SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
            Date currdate = new Date();
            Date enddate = dateformat.parse(sp.getString("enddate", null));
            SharedPreferences.Editor e = sp.edit();

            if (currdate.after(enddate))
            { //if past stop collecting and upload the data if on wifi

                //make the app stop collecting data from now on
                if (sp.getBoolean("consent", false)==true) {
                    e.putBoolean("consent", false);
                    e.commit();
                }

                //now try to upload
                if (sp.getBoolean("uploaded", false)==false)
                {
                    ConnectivityManager cm =
                            (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

                    if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                    {

                        //do the upload
                        if(!sbn.getPackageName().equals("com.notify.notifyme")) {
                            myDB = new MyDBHelper(getApplicationContext());
                            String dbpath = myDB.exportDatabase();
                            UploadDataTask task = new UploadDataTask(getApplicationContext(), this);
                            task.execute(dbpath);

                        }
                    }
                    else
                    {
                        //not in wifi
                        //do nothing!
                    }

                }
                else {

                }

                return;
            }
            else //date has passed
            {

            }
        }
        catch (Exception e)
        {
            return;
        }

        if(sp.getBoolean("consent", false)==false)
            return;

        int progress = 0;
        boolean indeterminate_progress = false;
        try
        {
            progress = sbn.getNotification().extras.getInt(Notification.EXTRA_PROGRESS);
            indeterminate_progress= sbn.getNotification().extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE);
        }
        catch (Exception e)
        {}

        Log.i("Notification progress", "progress = "+progress+" is_indeterm "+indeterminate_progress);

        if (progress!=0 || indeterminate_progress==true) //don't write it (it's an update)
            return;



        //---show current notification---
        sss=sbn;
        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        //test lock screen configuration
        //int show_all = Settings.Secure.getInt(getContentResolver(),"lock_screen_allow_private_notifications", -1);
        int noti_enabled = Settings.Secure.getInt(getContentResolver(),"lock_screen_show_notifications", -1);

        Log.i("Lock screen config", "Show notifs on lockscreen "+noti_enabled);


        CapturedNotification cn;

        if (Build.VERSION.SDK_INT >=23)
            cn = new CapturedNotification(sbn, am.getRingerMode(),
                                    pm.isInteractive(), pm.isDeviceIdleMode(), d.getState(),noti_enabled);
        else
                cn = new CapturedNotification(sbn, am.getRingerMode(),
                        pm.isScreenOn(), pm.isScreenOn(), d.getState(),noti_enabled);



        myDB = new MyDBHelper(this);
        myDB.insertData(cn);

        try {
            mPlaceDetectionClient= Places.getPlaceDetectionClient(getApplicationContext());
            Log.i("Google Places", "Starting task");
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener(new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                    //task completed
                    try{
                        //here are the results
                        PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();
                        //this is the best result
                        bestplace = likelyPlaces.get(0).getPlace();
                        Log.i(TAG,"Most likely: "+bestplace.getName()+"\n"+
                                bestplace.getAddress() + "\n"+likelyPlaces.get(0).getLikelihood());

                        for(int x : bestplace.getPlaceTypes())
                        {
                            Log.i("Best place", "category: "+x);
                        }
                        Log.i("Best Place",bestplace.getLatLng().toString());
                        myDB.updatePlace(sss.getId(),sss.getPostTime(),likelyPlaces.get(0));

                        //all possibilities
                        for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                            Log.i("Google Places", String.format("Place '%s' has likelihood: %g",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }

                        //release the result set to prevent memory leak
                        likelyPlaces.release();
                    }
                    catch (Exception e)
                    {
                        Log.e("Google Places", e.getMessage());
                        e.printStackTrace();
                    }
                }
            });

            Notification n = sbn.getNotification();
            Log.i("Notification","---Current Notification---");

            Log.i("Notification", "Id: " + sbn.getId());
            Log.i("Notification", "Priority: " +n.priority);
            Log.i("Notification", "tickerText: " +n.tickerText);
            Log.i("Notification", "AppName: " +sbn.getPackageName());
            Log.i("Notification", "TimePosted: " +sbn.getPostTime()/1000);
            Log.i("Notification", "Sound: " +n.sound);
            Log.i("Notification", "Ringer mode " + am.getRingerMode());
            Log.i("Notification", "Defaults " + n.defaults);

            Log.i("Notification","--------------------------");
        }
        catch (SecurityException e)
        {
            Log.e("Google Places", "no permission");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //---show all active notifications---
        /*
        Log.i("Notification","===All Notifications===");

        for (StatusBarNotification notif : this.getActiveNotifications()) {

            Log.i("Notification", "Id: " + notif.getId());
            Log.i("Notification", "tickerText: " + notif.getNotification().tickerText);
            Log.i("Notification", "AppName: " +notif.getPackageName() );
        }

        Log.i("","===aaa====================");
        */
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        if(sp.getBoolean("consent", false)==false)
            return;

        try {
            myDB = new MyDBHelper(this);
            Log.i("Notification", "---Notification Removed---");
            Log.i("Notification", "Id: " + sbn.getId());
            Log.i("Notification", "tickerText: " + sbn.getNotification().tickerText);
            Log.i("Notification", "AppName: " + sbn.getPackageName());
            Log.i("Notification", "Post time: " + sbn.getPostTime());
            Log.i("Notification", "---rrr-----------------------");
            if (sbn != null)
                myDB.updateRemoveTime(sbn, System.currentTimeMillis() / 1000);
            else
                Log.i(TAG, "removed sbn is null");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "error on notification removed");
        }

    }



    @Override
    public void onDestroy() {
        // The service is no longer used and is being destroyed
        super.onDestroy();
        unregisterReceiver(rs);
    }

    public void notifyToUninstall()
    {
        NotificationCompat.Builder notification;
        // Build the notification.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)
            notification = new NotificationCompat.Builder(getApplicationContext());
        else {
            NotificationChannel channel = new NotificationChannel("test123", getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, "test123" );
        }

        notification.setAutoCancel(true); // Whenever clicked a notification, it is deleted from the top system status bar.
        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setTicker(getString(R.string.app_name));
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle(getString(R.string.app_name)+" experiment is now over!");
        notification.setContentText("Click this notification to uninstall the NotifyMe application. Thank you for taking part!");
        notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notification.setDefaults(Notification.DEFAULT_SOUND);

        // Create an explicit intent for an Activity in your app

        Uri packageUri = Uri.parse("package:" + "com.notify.notifyme");
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(),0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //startActivity(intent);
        notification.addAction(R.drawable.ic_launcher, "Uninstall", pi);
        notification.setContentIntent(pi);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(123192, notification.build());
    }
}