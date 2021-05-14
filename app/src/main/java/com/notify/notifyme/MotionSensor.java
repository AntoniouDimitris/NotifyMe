package com.notify.notifyme;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.PendingIntent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import android.view.View;
import android.preference.PreferenceManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MotionSensor extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityRecognitionClient mActivityRecognitionClient;
    private Context mContext;
    private ArrayAdapter<String> arrayAdapter;
    private PendingIntent globalIntent;
    private boolean checked = false;
    private String s;
    private LocationManager locationManager;
    private ArrayList<String> globalTrackedActivities;
    //public static boolean bicycle = false, foot = false, running = false, still = false, tilting = false, unknown = false, vehicle = false, walking = false;

    SharedPreferences sharedpreferences;
    Dialog dialog;

    Button trackedActivitiesBtn;
    ListView activitiesListview;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motion_sensor);

        mContext = this;
        trackedActivitiesBtn = findViewById(R.id.select_tracking_btn);
        activitiesListview = findViewById(R.id.activities_list);
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mActivityRecognitionClient = ActivityRecognition.getClient(this);

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        Intent intent = new Intent(MotionSensor.this, MainActivity.class);
        startActivity(intent);
    }

    public void stopActivitiesUpdates() {

        Task<Void> taskStop = mActivityRecognitionClient.removeActivityUpdates(globalIntent);

        taskStop.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Taskstop success", "stopped?");
            }
        });
    }

    public void requestUpdatesHandler(View view) {

        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled( LocationManager.GPS_PROVIDER )) {
            startLocationService();
        }
        else { Toast.makeText(this, "MotionSensor started. \n Location updates failed to start. \n Try again with GPS enabled.", Toast.LENGTH_LONG).show(); }

        globalIntent = getPendingIntent();

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                2000,
                globalIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.i("Task success", "tracking");
            }
        });

        String dumtext = "Tracking for: None";
        if (trackedActivitiesBtn.getText().toString().equals(dumtext))
        {
            trackedActivitiesBtn.setText("Tracking for: All");
        }
    }

    private PendingIntent getPendingIntent() {

        Intent intent = new Intent(this, ActivityRecognitionService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }

    @Override
    protected void onResume() {

        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
        updateList();
    }

    @Override
    protected void onPause() {

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }


    public void Tracker(View view) {

        final ArrayList<String> trackedActivities = new ArrayList<>();
        final String[] items = {"None", "All", getString(R.string.in_vehicle), getString(R.string.on_bicycle), getString(R.string.on_foot), getString(R.string.still), getString(R.string.tilting), getString(R.string.walking), getString(R.string.running)};
        final ArrayList<Integer> itemsSelected = new ArrayList();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select activities you want to track: ");
        builder.setMultiChoiceItems(items, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int selectedItemID, boolean isSelected) {

                if (isSelected) {
                    itemsSelected.add(selectedItemID);

                    if (itemsSelected.contains(0) && itemsSelected.size() > 1) {

                        itemsSelected.clear();
                        AlertDialog.Builder warning = new AlertDialog.Builder(MotionSensor.this);
                        warning.setTitle("You cannot select None along with another option!  Try again.");
                        warning.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        AlertDialog warningDialog = warning.create();
                        warningDialog.show();
                        dialog.dismiss();
                    }
                } else if (itemsSelected.contains(selectedItemID)) {
                    itemsSelected.remove(Integer.valueOf(selectedItemID));
                }
            }
        })

                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        for (int i = 0; i < itemsSelected.size(); i++) {

                            trackedActivities.add(items[itemsSelected.get(i)]);
                        }

                        s = trackedActivities.toString();
                        s = s.replaceAll("[\\[\\]]", "");
                        trackedActivitiesBtn.setText("Tracking for: " + s);

                        if (itemsSelected.contains(0)) {
                            stopActivitiesUpdates();
                        } else if (itemsSelected.contains(1)) {
                            checked = false;
                        } else {
                            checked = true;
                            globalTrackedActivities = trackedActivities;
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {

                    }
                });
        dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Destroyed", "destroyed");
        stopActivitiesUpdates();
    }

   /* protected static void passActivities(boolean b, boolean f, boolean r, boolean s, boolean t, boolean u, boolean v, boolean w) {

        bicycle = b;
        foot = f;
        running = r;
        still = s;
        tilting = t;
        unknown = u;
        vehicle = v;
        walking = w;
    }*/

    public void startLocationService() {
        Intent serviceIntent = new Intent(this, LocationService.class);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    private void updateList() {

        int count = sharedpreferences.getInt("Counter", 0);
        ArrayList<String> actList = new ArrayList();
        String valueAdd;
        String prob;

        for (int i = 1; i <= count; i++) {

            StringBuilder s = new StringBuilder(500);
            String iStr = String.valueOf(i);
            valueAdd = sharedpreferences.getString(getString(R.string.typeKey) + iStr, "");
            prob = String.valueOf(sharedpreferences.getInt(getString(R.string.probKey) + iStr, 0));
            s.append(valueAdd + "\t\t" + prob + "%");

            actList.add(s.toString());
        }

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.textviews, R.id.adapterView, actList);
        activitiesListview.setAdapter(arrayAdapter);

    }

    private void updateList(ArrayList<String> trackedActivities) {
        int count = sharedpreferences.getInt("Counter", 0);
        ArrayList<String> actList = new ArrayList();
        String valueAdd;
        String prob;

        for (int i = 1; i <= count; i++) {

            String iStr = String.valueOf(i);

            for (int k = 0; k < trackedActivities.size(); k++) {
                if (sharedpreferences.getString(getString(R.string.typeKey) + iStr, "") == trackedActivities.get(k)) {
                    if (sharedpreferences.getInt(getString(R.string.probKey) + iStr, 0) >= 60) {
                        StringBuilder s = new StringBuilder(500);
                        valueAdd = sharedpreferences.getString(getString(R.string.typeKey) + iStr, "");
                        prob = String.valueOf(sharedpreferences.getInt(getString(R.string.probKey) + iStr, 0));
                        Log.d("Activity_Meta", iStr);
                        s.append(valueAdd + "\t\t" + prob + "%");
                        actList.add(s.toString());
                    }
                } else {
                    continue;
                }
            }

        }

        arrayAdapter = new ArrayAdapter<String>(this, R.layout.textviews, R.id.adapterView, actList);
        activitiesListview.setAdapter(arrayAdapter);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String update) {

        if (!checked) {
            if (update.equals("Counter")) {
                arrayAdapter.clear();
                updateList();
            }
        } else {
            if (update.equals("Counter")) {
                arrayAdapter.clear();
                Log.d("Activity_size", String.valueOf(globalTrackedActivities.size()));
                updateList(globalTrackedActivities);
            }
        }
    }
}
