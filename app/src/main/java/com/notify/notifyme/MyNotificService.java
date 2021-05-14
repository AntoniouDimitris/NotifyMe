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
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.libraries.places.api.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class MyNotificService extends NotificationListenerService
{
    MyDBHelper myDB;
    private final String TAG = "Notification Service";
    private String apiKey = "AIzaSyAyxBgqAdAEtaxvdMtDcx7SR2wSv8vO5wU";

    private StatusBarNotification sss;
    private RingerChangedReceiver rs;
    private SharedPreferences sp;


    public void onCreate()
    {
        super.onCreate();
        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Log.d(TAG, "toggleNotificationListenerService() called");
        ComponentName thisComponent = new ComponentName(this, /*getClass()*/ MyNotificService.class);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(thisComponent, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //set up receivers
        rs = new RingerChangedReceiver();
        registerReceiver(rs, new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION));

        //set up uploading service
        Intent alarmIntent = new Intent(this, UploadDataReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        //alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis()+60*1000, 60*1000, pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.RTC, Calendar.getInstance().getTimeInMillis(), AlarmManager.INTERVAL_HOUR*6, pendingIntent);
        //alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 60*1000, pendingIntent);
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_HOUR*6, pendingIntent);

        //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 3000, pendingIntent );


        Log.i(TAG, "Alarm set ");
        Log.i(TAG,"OnCreate complete");

    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {


        Log.i("Tralalaal", "tralalaeooeoele " +sbn.getPackageName());
        Log.i("Coordinates", String.valueOf(LocationService.userLat) + " " + String.valueOf(LocationService.userLong) + " " + String.valueOf(LocationService.speed));

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
                        //not in wif, do nothing!
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
        {
        }

        Log.i("Notification progress", "progress = "+progress+" is_indeterm "+indeterminate_progress);

        if (progress!=0 || indeterminate_progress==true) //don't write it (it's an update)
            return;



        //---show current notification---
        sss=sbn;
        AudioManager am = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        PowerManager pm = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (sbn.getId() == 4952) {
            SharedPreferences.Editor e = sp.edit();
            e.putLong("driving", sbn.getPostTime());
            e.commit();
        }
        //test lock screen configuration
        //int show_all = Settings.Secure.getInt(getContentResolver(),"lock_screen_allow_private_notifications", -1);
        int noti_enabled = Settings.Secure.getInt(getContentResolver(),"lock_screen_show_notifications", -1);

        Log.i("Lock screen config", "Show notifs on lockscreen "+noti_enabled);

        //get most probable activities
        ArrayList<Boolean> activities = new ArrayList<>();
        activities.add(ActivityRecognitionService.checkBicycle);
        activities.add(ActivityRecognitionService.checkFoot);
        activities.add(ActivityRecognitionService.checkRunning);
        activities.add(ActivityRecognitionService.checkStill);
        activities.add(ActivityRecognitionService.checkTilting);
        activities.add(ActivityRecognitionService.checkUnknown);
        activities.add(ActivityRecognitionService.checkWalking);
        activities.add(ActivityRecognitionService.checkVehicle);

        /*if(ActivityRecognitionService.checkBicycle) {
            Toast.makeText(this, "Bicycle" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }
        if(ActivityRecognitionService.checkStill) {
            Toast.makeText(this, "Still" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }
        if(ActivityRecognitionService.checkRunning) {
            Toast.makeText(this, "Running" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }
        if(ActivityRecognitionService.checkWalking) {
            Toast.makeText(this, "Walking" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }
        if(ActivityRecognitionService.checkVehicle) {
            Toast.makeText(this, "Vehicle" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }
        if(ActivityRecognitionService.checkUnknown) {
            Toast.makeText(this, "Unknown" + " " + LocationService.speed, Toast.LENGTH_SHORT).show();
        }*/

        Log.i("Activitiez0", String.valueOf(activities.get(0)));
        Log.i("Activitiez1", String.valueOf(activities.get(1)));
        Log.i("Activitiez2", String.valueOf(activities.get(2)));
        Log.i("Activitiez3", String.valueOf(activities.get(3)));
        Log.i("Activitiez4", String.valueOf(activities.get(4)));
        Log.i("Activitiez5", String.valueOf(activities.get(5)));
        Log.i("Activitiez6", String.valueOf(activities.get(6)));
        Log.i("Activitiez7", String.valueOf(activities.get(7)));

        CapturedNotification cn;

        if (activities.get(3)) {
            Log.i("Speed", "midenistike");
            LocationService.speed = 0;
        }

        if (Build.VERSION.SDK_INT >=23) {
            cn = new CapturedNotification(sbn, am.getRingerMode(),
                    pm.isInteractive(), pm.isDeviceIdleMode(), d.getState(), noti_enabled, activities, LocationService.speed, LocationService.userLat, LocationService.userLong);
        }else {
            cn = new CapturedNotification(sbn, am.getRingerMode(),
                    pm.isScreenOn(), pm.isScreenOn(), d.getState(), noti_enabled, activities, LocationService.speed, LocationService.userLat, LocationService.userLong);
        }

        myDB = new MyDBHelper(this);
        myDB.insertData(cn);

        if (!Places.isInitialized()) {
            Log.i("Komple", "dor");
            Places.initialize(getApplicationContext(), apiKey);
            Log.i("Komple", "qq");
        }

        PlacesClient placesClient = Places.createClient(this);
        if(Places.isInitialized()) {
            Log.i("Komple", "dor");
        }
        else {
            Log.i("Komple", "qq");
        }

        try {
            // Use fields to define the data types to return.
            List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME, Place.Field.TYPES, Place.Field.LAT_LNG, Place.Field.ID);

            // Use the builder to create a FindCurrentPlaceRequest.
            FindCurrentPlaceRequest request =
                    FindCurrentPlaceRequest.builder(placeFields).build();

            // Call findCurrentPlace and handle the response (first check that the user has granted permission).
            placesClient.findCurrentPlace(request).addOnSuccessListener(((response) -> {
                for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {

                    Log.i("Placezz", String.format("Place '%s' has likelihood: %f, type: %s, lat: %,.8f, lon: %,.8f, ID: %s",
                            placeLikelihood.getPlace().getName(),
                            placeLikelihood.getLikelihood(),
                            placeLikelihood.getPlace().getTypes(),
                            placeLikelihood.getPlace().getLatLng().latitude,
                            placeLikelihood.getPlace().getLatLng().longitude,
                            placeLikelihood.getPlace().getId()));
                }
                Log.i("Placezz", response.getPlaceLikelihoods().get(0).getPlace().getName());
                Log.i("Placezz", String.format("Likelihood %f", response.getPlaceLikelihoods().get(0).getLikelihood()));
                PlaceLikelihood bestplace = response.getPlaceLikelihoods().get(0);
                myDB.updatePlace(sss.getId(), sss.getPostTime(), bestplace.getPlace().getName(), bestplace.getLikelihood(), bestplace.getPlace().getTypes().toString(), bestplace.getPlace().getLatLng().latitude, bestplace.getPlace().getLatLng().longitude, bestplace.getPlace().getId());
            })).addOnFailureListener((exception) -> {
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //int handle = 0;

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
            if (sbn != null) {
                myDB.updateRemoveTime(sbn, System.currentTimeMillis());
                if (sbn.getPackageName().equals("com.notify.notifyme")) {
                    myDB.updateQuestionnaireNotifRemoved(sp.getLong("lastinsertid", -1), 0,System.currentTimeMillis() / 1000);
                }
            }else {
                Log.i(TAG, "removed sbn is null");
            }
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
        Log.i("DestroyedNotif", "destroyed");
        unregisterReceiver(rs);
    }

    public void notifyToUninstall()
    {
        NotificationCompat.Builder notification;
        // Build the notification.
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            notification = new NotificationCompat.Builder(getApplicationContext());
        else {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel channel = notificationManager.getNotificationChannel("ABCDEF");
            if(channel==null) {
                CharSequence name = "NotifyMeChannel";
                String description = "QuietSense experiment";
                int importance = NotificationManager.IMPORTANCE_HIGH;
                channel = new NotificationChannel("ABCDEF", name, importance);
                channel.setDescription(description);
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(channel);
            }
            notification = new NotificationCompat.Builder(this, "ABCDEF" );
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