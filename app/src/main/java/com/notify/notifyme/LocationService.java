package com.notify.notifyme;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.math.RoundingMode;
import java.math.BigDecimal;

public class LocationService extends Service implements android.location.LocationListener {

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 1;
    private LocationManager locationManager;
    public static double speed = 0;
    public static double userLat;
    public static double userLong;
    private Location previousLocation;
    private PendingIntent alarmIntent;
    private AlarmManager am;

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("DestroyedLocation", "destroyed");
        locationManager.removeUpdates(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        Log.i("LocationService", "started");
        Intent intent = new Intent(this, LocationServiceReceiver.class);
        intent.setAction("ALRMTOSEND");
        alarmIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime(), AlarmManager.INTERVAL_HOUR*12, alarmIntent);

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_channel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(1, notification);
        }

        try {
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            if (locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                }
                else {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("LocationService", "changed");
        if (location != null) {
            userLat = round(location.getLatitude(), 7);
            userLong = round(location.getLongitude(), 7);
            if (previousLocation != null) {
                speed = Math.sqrt(
                        Math.pow(location.getLongitude() - previousLocation.getLongitude(), 2)
                                + Math.pow(location.getLatitude() - previousLocation.getLatitude(), 2)
                ) / (location.getTime() - previousLocation.getTime());
                //DecimalFormat df = new DecimalFormat("#.###");
               //speed = Double.parseDouble(df.format(speed));
            }
            //if there is speed from location
            if (location.hasSpeed() && location.getSpeed() > 0) {
                //get location speed
                speed = location.getSpeed();
                //DecimalFormat df = new DecimalFormat("#.##");
                //speed = Double.parseDouble(df.format(speed));
            }
            previousLocation = location;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
