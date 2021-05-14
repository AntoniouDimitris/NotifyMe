package com.notify.notifyme;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity
{
    MyDBHelper myDBHelper;

    NotificationCompat.Builder notification;
    private static final int uniqueID = 45612;

    private Button btnexportDatabase;
    private Button permissionButton;
    private Button appSettingsButton;
    private Button sensor;
    private Button uploadDBButton;
    private Button notifyButton;
    private Button batteryPermButton;
    private TextView notificationstext;
    private TextView permtext;
    private Button saveButton;
    private EditText username;
    private SharedPreferences sp;

    private final int REQUEST_LOCATION = 2;
    private final int REQUEST_WRITE = 3;
    private final int REQUEST_ID = 4;

    private static boolean mLocationPermissionGranted = false;
    private static boolean mWritePermissionGranted = false;
    private static boolean mIdPermissionGranted = false;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        toggleNotificationListenerService();
        locationEnabled();
        //check package code
        try {
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);
            Log.e("version code", "v."+packageInfo.versionCode);

            if(packageInfo.versionCode<=5) //send any collected data
            {

            }

        }catch (Exception ex)
        {

        }

        //set the remove date


        sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(sp.getString("enddate", null)==null) { //if end date not set

            SharedPreferences.Editor e = sp.edit();
            SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
            Date currdate = new Date();
            Calendar c = Calendar.getInstance();
            try {
                c.setTime(new Date());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            c.add(Calendar.DATE, 30);  // number of days to add, can also use Calendar.DAY_OF_MONTH in place of Calendar.DATE

            e.putString("enddate", dateformat.format(c.getTime()));
            e.commit();
            Log.e("created end date", dateformat.format(c.getTime()));
        }
        else
            Log.e("created end date", "date already set "+sp.getString("enddate", null));


        myDBHelper = new MyDBHelper(this);

        notifyButton = (Button) findViewById(R.id.clickMeButton);
        notifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickMeButtonClicked();
            }
        });
        notifyButton.setVisibility(View.VISIBLE);

        uploadDBButton = (Button) findViewById(R.id.upload);
        uploadDBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDBHelper = new MyDBHelper(getApplicationContext());
                //myDBHelper.exportDatabase();
                String dbpath = myDBHelper.exportDatabase();
                //UploadDataTask task = new UploadDataTask(getApplicationContext());
                //task.execute(dbpath);
            }
        });
        uploadDBButton.setVisibility(View.GONE);

        permissionButton = (Button) findViewById(R.id.permissionButton);
        permissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(intent);
            }
        });

        /*
        batteryPermButton = (Button) findViewById(R.id.buttonbatteryPermission);
        batteryPermButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                       Uri.parse("package:"+getPackageName())));

            }
        });
        batteryPermButton.setVisibility(View.GONE);
        */


        appSettingsButton = (Button) findViewById(R.id.appSettingsButton);
        appSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            }
        });

        sensor = (Button) findViewById(R.id.sensor_button);
        sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sensIntent = new Intent(getApplicationContext(), MotionSensor.class);
                startActivity(sensIntent);
            }
        });

        btnexportDatabase = (Button) findViewById(R.id.export);


        btnexportDatabase.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //export the database with table here
                myDBHelper = new MyDBHelper(getApplicationContext());
                myDBHelper.exportDatabase();

            }
        });
        btnexportDatabase.setVisibility(View.GONE);

        notificationstext = (TextView) findViewById(R.id.notifications);
        permtext = (TextView) findViewById(R.id.permissions);


        username = (EditText) findViewById(R.id.username);
        username.setText(sp.getString("username", "none"));

        username.setVisibility(View.GONE);

        saveButton = (Button) findViewById(R.id.saveusername);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor e = sp.edit();
                e.putString("username",username.getText().toString());
                e.commit();
                Toast.makeText(getApplicationContext(), "Saved successfully", Toast.LENGTH_SHORT).show();
            }
        });
        saveButton.setVisibility(View.GONE);

        handleUserConsent();

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            //Log.e("running services", service.service.getClassName());
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MyDBHelper db = new MyDBHelper(this);
        notificationstext.setText(db.getNotifications());
        checkLocationPermission();
        checkWritePermission();
        checkIdPermission();
        permtext.setText("Permissions write "+mWritePermissionGranted+
                ", location "+mLocationPermissionGranted+", id "+mIdPermissionGranted);
        permtext.setVisibility(View.GONE);
        Log.e("IS SERVICE RUNNING", "result: "+isMyServiceRunning(MyNotificService.class));
    }

    private void handleUserConsent()
    {
        //handle user consent
        Log.i("consent form", "initial user consent "+sp.getBoolean("consent", false));
        if(sp.getBoolean("consent", false)==false && sp.getBoolean("uploaded", false)==false) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            //      //Add the buttons
            builder.setPositiveButton("I agree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    SharedPreferences.Editor e = sp.edit();
                    e.putBoolean("consent", true);
                    e.commit();
                    Log.i("consent form", "user consent "+sp.getBoolean("consent", false));
                }
            });
            builder.setNegativeButton("I don't agree", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    SharedPreferences.Editor e = sp.edit();
                    e.putBoolean("consent", false);
                    e.commit();
                    Log.i("consent form", "user consent "+sp.getBoolean("consent", false));
                }
            });

            builder.setMessage("QuietSense is a research application to help us collect data about how people handle their incoming notifications. " +
                    "By installing and continuing to use the application, you agree that the following information will be collected while you use " +
                    "your phone and will be uploaded to our server when we ask you to do so. " +
                    "(a) Your device serial number (as specified in Settings>System>About Phone>Status)\n" +
                    "\n" +
                    "(b) The time of issuing and dismissal of any incoming notifications on your device\n" +
                    "\n" +
                    "(c) The application names that issue notifications on your device\n" +
                    "\n" +
                    "(d) Your device's ringer mode\n" +
                    "\n" +
                    "(e) Information on whether the incoming notification had programmed vibration, status led and sound, and the notification programmed priority\n" +
                    "\n" +
                    "(f) Your approximate location at the time when a notification is received, as resolved to a place name via the Google Places API.\n" +
                    "\n" +
                    "")

                    .setTitle("CONSENT NOTICE!");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void checkLocationPermission()
    {
        //check current permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
        {
            //Not granted, ask for permissions now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
        } else {
            //permission already granted
            mLocationPermissionGranted = true;

        }
    }

    public void checkWritePermission()
    {
        //check current permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            //Not granted, ask for permissions now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE);
        } else {
            //permission already granted
            mWritePermissionGranted = true;

        }
    }
    public void checkIdPermission()
    {
        //check current permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
        {
            //Not granted, ask for permissions now
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    REQUEST_ID);
        } else {
            //permission already granted
            mIdPermissionGranted = true;

        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        boolean endvalue = false;
        if(grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            endvalue = true;
        }

        switch (requestCode) {
            case REQUEST_LOCATION:
                mLocationPermissionGranted = endvalue;
                break;
            case REQUEST_WRITE:
                mWritePermissionGranted = endvalue;
                break;
            case REQUEST_ID:
                mIdPermissionGranted = endvalue;
                break;
        }
        permtext.setText("Permissions write "+mWritePermissionGranted+
                ", location "+mLocationPermissionGranted+", id "+mIdPermissionGranted);
        permtext.setVisibility(View.GONE);
    }

    public void clickMeButtonClicked()
    {
        Log.e("IS SERVICE RUNNING", "result: "+isMyServiceRunning(MyNotificService.class));
        Toast.makeText(this, "Service Running: " + String.valueOf(isMyServiceRunning(MyNotificService.class)), Toast.LENGTH_LONG) .show();

        // Build the notification.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O)
            notification = new NotificationCompat.Builder(MainActivity.this);
        else {
            NotificationChannel channel = new NotificationChannel("test123", "NotifyMe", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);
            notification = new NotificationCompat.Builder(this, "test123" );
        }

        notification.setAutoCancel(true); // Whenever clicked a notification, it is deleted from the top system status bar.
        notification.setSmallIcon(R.drawable.ic_launcher);
        notification.setTicker("HELLO BOSS !");
        notification.setWhen(System.currentTimeMillis());
        notification.setContentTitle("Sunday party.");
        notification.setContentText("UNIVERSITY OF PATRAS");
        notification.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notification.setDefaults(Notification.DEFAULT_SOUND);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(MainActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, intent, 0);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        // Builds notification and issues it.
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(uniqueID, notification.build());
    }

    private void locationEnabled () {

        LocationManager lm = (LocationManager)
                getSystemService(Context. LOCATION_SERVICE ) ;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager. GPS_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        try {
            network_enabled = lm.isProviderEnabled(LocationManager. NETWORK_PROVIDER ) ;
        } catch (Exception e) {
            e.printStackTrace() ;
        }
        if (gps_enabled == false) {
            new AlertDialog.Builder(MainActivity. this )
                    .setMessage( "GPS is turned off. Please enable GPS(High Accuracy) to use motion tracking." )
                    .setPositiveButton( "Settings" , new
                            DialogInterface.OnClickListener() {
                                @Override
                                public void onClick (DialogInterface paramDialogInterface , int paramInt) {
                                    startActivity( new Intent(Settings. ACTION_LOCATION_SOURCE_SETTINGS )) ;
                                }
                            })
                    .setNegativeButton( "Cancel" , null )
                    .show() ;
        }
    }

    private void toggleNotificationListenerService() {
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, com.notify.notifyme.NotificationCollectorMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, com.notify.notifyme.NotificationCollectorMonitorService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }
}

