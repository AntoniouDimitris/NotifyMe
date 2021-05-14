package com.notify.notifyme;

import android.app.IntentService;

import java.util.ArrayList;
import java.lang.reflect.Type;
import java.util.List;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AppCompatActivity;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.IntentService;
import android.app.NotificationManager;
import android.media.AudioManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityRecognitionService extends IntentService {

    public static volatile boolean shouldContinue = true;
    protected static final String TAG = "Activity";
    SharedPreferences sharedpreferences;
    String sType = "";
    private int tempCount;
    private int count = 0;
    private ArrayList<String> tempActivityWindow;
    private ArrayList<Integer> confidenceVerifier;
    private ArrayList<Boolean> checks;

    public static boolean checkBicycle = false;
    public static boolean checkFoot = false;
    public static boolean checkRunning = false;
    public static boolean checkStill = false;
    public static boolean checkTilting = false;
    public static boolean checkUnknown = false;
    public static boolean checkWalking = false;
    public static boolean checkVehicle = false;


    public ActivityRecognitionService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        checkBicycle = false;
        checkFoot = false;
        checkRunning = false;
        checkStill = false;
        checkTilting = false;
        checkUnknown = false;
        checkVehicle = false;
        checkWalking = false;

        Log.d("Activity_Recognized", "----------------");
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        tempActivityWindow = new ArrayList<>();
        confidenceVerifier = new ArrayList<>();

        checks = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            checks.add(sharedpreferences.getBoolean("Activity_check" + String.valueOf(i), false));
        }

        Log.i("Array_size", String.valueOf(checks.size()));

        Log.i("Arrayzz0", String.valueOf(checks.get(0)));
        Log.i("Arrayzz1", String.valueOf(checks.get(1)));
        Log.i("Arrayzz2", String.valueOf(checks.get(2)));
        Log.i("Arrayzz3", String.valueOf(checks.get(3)));
        Log.i("Arrayzz4", String.valueOf(checks.get(4)));
        Log.i("Arrayzz5", String.valueOf(checks.get(5)));
        Log.i("Arrayzz6", String.valueOf(checks.get(6)));
        Log.i("Arrayzz7", String.valueOf(checks.get(7)));

        count = sharedpreferences.getInt("Counter", 0);

        for (int i = 1; i <= count; i++) {

            String iStr = String.valueOf(i);
            tempActivityWindow.add(sharedpreferences.getString((getString(R.string.typeKey) + iStr), ""));
            confidenceVerifier.add(sharedpreferences.getInt((getString(R.string.probKey) + iStr), 0));
            Log.i("Dummies", tempActivityWindow.get(i - 1) + String.valueOf(confidenceVerifier.get(i - 1)));
        }

        if (ActivityRecognitionResult.hasResult(intent)) {

            ActivityRecognitionResult activitiesResult = ActivityRecognitionResult.extractResult(intent);
            sortDetectedActivities(activitiesResult.getProbableActivities());
        }
    }

    private void sortDetectedActivities(List<DetectedActivity> detectedActivities) {

        SharedPreferences.Editor editor = sharedpreferences.edit();

        tempCount = 1;
        for (DetectedActivity x : detectedActivities) {
            int type = x.getType();
            switch (type) {
                case 0: {
                    Log.d("Activity_Recognized", "In Vehicle: " + x.getConfidence());
                    sType = getString(R.string.in_vehicle);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(6)) && x.getConfidence() >= 55) {
                            checkVehicle = true;
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                AudioManager am = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                                if (System.currentTimeMillis() > sharedpreferences.getLong("LAST_CONFIG_CHECK_DATE", 0) + 180000) {
                                    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
                                        sendNotif();
                                        editor.putLong("LAST_CONFIG_CHECK_DATE", System.currentTimeMillis());
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case 1: {
                    Log.d("Activity_Recognized", "On Bicycle: " + x.getConfidence());
                    sType = getString(R.string.on_bicycle);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(0)) && x.getConfidence() >= 55) {
                            checkBicycle = true;
                        }
                    }
                    break;
                }
                case 2: {
                    Log.d("Activity_Recognized", "On Foot: " + x.getConfidence());
                    sType = getString(R.string.on_foot);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(1)) && x.getConfidence() >= 55) {
                            checkFoot = true;
                        }
                    }
                    break;
                }
                case 3: {
                    Log.d("Activity_Recognized", "Still: " + x.getConfidence());
                    sType = getString(R.string.still);
                    for (int i = 0; i <= count - 1; i++) {
                        Log.i("Whyyyy", "not");
                        if ((tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(3)) && x.getConfidence() >= 55) || x.getConfidence() >=99) {
                            checkStill = true;
                            Log.d("Activity_confirmed", "Found!");
                        }
                    }
                    break;
                }
                case 4: {
                    Log.d("Activity_Recognized", "Unknown: " + x.getConfidence());
                    sType = getString(R.string.unknown);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && confidenceVerifier.get(i) >= 55 && x.getConfidence() >= 55) {
                            checkUnknown = true;
                        }
                    }
                    //fix unknown state
                    if (x.getConfidence() == 40) {
                        if (!checks.isEmpty()) {
                            if (checks.get(0)) {
                                checkBicycle = true;
                            } else if (checks.get(1)) {
                                checkFoot = true;
                            } else if (checks.get(2)) {
                                checkRunning = true;
                            } else if (checks.get(3)) {
                                checkStill = true;
                                Log.i("checkStill", "true");
                            } else if (checks.get(4)) {
                                checkTilting = true;
                            } else if (checks.get(5)) {
                                checkUnknown = true;
                            } else if (checks.get(6)) {
                                checkVehicle = true;
                            } else if (checks.get(7)) {
                                checkWalking = true;
                            }
                        }
                    }
                    break;
                }
                case 5: {
                    Log.d("Activity_Recognized", "Tilting: " + x.getConfidence());
                    sType = getString(R.string.tilting);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(4)) && x.getConfidence() >= 55) {
                            checkTilting = true;
                        }
                    }
                    break;
                }
                case 7: {
                    Log.d("Activity_Recognized", "Walking: " + x.getConfidence());
                    sType = getString(R.string.walking);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(7)) && x.getConfidence() >= 55) {
                            checkWalking = true;
                        }
                    }
                    break;
                }
                case 8: {
                    Log.d("Activity_Recognized", "Running: " + x.getConfidence());
                    sType = getString(R.string.running);
                    for (int i = 0; i <= count - 1; i++) {
                        if (tempActivityWindow.get(i) == sType && (confidenceVerifier.get(i) >= 55 || checks.get(2)) && x.getConfidence() >= 55) {
                            checkRunning = true;
                        }
                    }
                    break;
                }
            }

            String countStr = String.valueOf(tempCount);
            editor.putString(getString(R.string.typeKey) + countStr, sType);
            editor.putInt(getString(R.string.probKey) + countStr, x.getConfidence());
            editor.putInt("Counter", tempCount);
            tempCount++;
        }

        editor.putBoolean("Activity_check" + String.valueOf(0), checkBicycle);
        editor.putBoolean("Activity_check" + String.valueOf(1), checkFoot);
        editor.putBoolean("Activity_check" + String.valueOf(2), checkRunning);
        editor.putBoolean("Activity_check" + String.valueOf(3), checkStill);
        editor.putBoolean("Activity_check" + String.valueOf(4), checkTilting);
        editor.putBoolean("Activity_check" + String.valueOf(5), checkUnknown);
        editor.putBoolean("Activity_check" + String.valueOf(6), checkVehicle);
        editor.putBoolean("Activity_check" + String.valueOf(7), checkWalking);

        //MotionSensor.passActivities(checkBicycle, checkFoot, checkRunning, checkStill, checkTilting, checkUnknown, checkVehicle, checkWalking);
        editor.commit();
    }


    // Notification: When driving, suggest silent mode
    private void sendNotif() {

        Intent intent = new Intent(querySettingPkgName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent yesIntent = new Intent(this, ActionReceiver.class);
        yesIntent.setAction("action1");
        PendingIntent pendingIntentYes = PendingIntent.getBroadcast(this, 0, yesIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent noIntent = new Intent(this, ActionReceiver.class);
        noIntent.setAction("action2");
        PendingIntent pendingIntentNo = PendingIntent.getBroadcast(this, 0, noIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder drivingNotif = new NotificationCompat.Builder(this, "kappa")
                .setSmallIcon(R.drawable.runner)
                .setContentIntent(pendingIntent)
                .setContentTitle("Reminder")
                .setContentText("You may want to turn your phone silent if driving")
                .setColor(0xDBACBD26)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.small_runner, "Yes", pendingIntentYes)
                .addAction(R.drawable.small_runner, "No, thanks", pendingIntentNo);

        CharSequence name = "MotionSensor";
        String description = "Nothing really";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel sensorChannel = new NotificationChannel("kappa", name, importance);
        sensorChannel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(sensorChannel);

        notificationManager.notify(4952, drivingNotif.build());
    }

    private String querySettingPkgName() {
        Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
        List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfos == null || resolveInfos.size() == 0) {
            return "";
        }

        return resolveInfos.get(0).activityInfo.packageName;
    }

}

