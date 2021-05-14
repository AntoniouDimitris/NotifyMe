package com.notify.notifyme;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.location.places.PlaceLikelihood;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String database_name = "notification.db";

    private String notif_table_name = "notification_table";
    private String ringer_table_name = "ringer_table";

    private static final int database_version = 2;

    private final String tag = "DB Helper";

    private String column_userid = "user_id";
    private String column_id = "id";
    private String column_nid = "nid";
    private String column_priority = "priority";
    private String column_packageName = "packageName";
    private String column_timePosted = "timePosted";
    private String column_timeRemoved = "timeRemoved";
    private String column_sound = "sound";
    private String column_defaultSound = "default_Sound";
    private String column_LED = "LED";
    private String column_defaultLED = "default_LED";
    private String column_vibrationPattern = "VibrationPattern" ;
    private String column_defaultVibration = "default_Vibration" ;
    private String column_RingerMode = "ringerMode";
    private String column_PlaceName = "PlaceName";
    private String column_PlaceId = "PlaceId";
    private String column_PlaceCategories = "PlaceCategories";
    private String column_timestamp = "eventtime";
    private String column_PlaceConfidence = "place_confidence";
    private String column_lat = "lat";
    private String column_lng = "lng";
    private String column_idleMode = "idle";
    private String column_interactive = "interactive";
    private String column_scrstate = "screen_state";
    private String column_notification_flags = "flags";
    private String column_lock_screen_allow_notifs = "lock_scr_notifs";
    private String backupDBname;
    private String deviceID;

    private String myapppkg;
    private Context ctx;

    public MyDBHelper(Context context) {
        super(context, database_name, null, database_version);
        ctx=context;
        SQLiteDatabase db = this.getWritableDatabase();
        setBackupDBname();

    }

    private void setBackupDBname()
    {
        try{
            myapppkg = ctx.getPackageName();
            final TelephonyManager tm = (TelephonyManager) ctx.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            SharedPreferences.Editor e = sp.edit();
            e.putString("username", tm.getDeviceId());
            e.commit();
            backupDBname = "nm"+"_"+sp.getString("username", "none")+".db";
        }
        catch (SecurityException e)
        {
            backupDBname = "notifyme.db";
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(" create table " + notif_table_name + "( "
                        + column_userid + " integer default null, "
                        + column_id + " integer primary key autoincrement, "
                        + column_nid + " integer default null, "
                        + column_priority + " integer default null, "
                        + column_packageName + " text not null ,"
                        + column_timePosted + " integer default null , "
                        + column_timeRemoved + " integer default null, "
                        + column_sound + " integer default null, "
                        + column_defaultSound + " integer default null, "
                        + column_LED + " integer default null, "
                        + column_defaultLED + " integer default null, "
                        + column_vibrationPattern + " text not null, "
                        + column_defaultVibration + " integer default null, "
                        + column_RingerMode + " integer default null,"
                        + column_PlaceName + " text default null, "
                        + column_PlaceId + " text default null, "
                        + column_PlaceConfidence + " real default null, "
                        + column_PlaceCategories + " text default null, "
                        + column_lat + " real default null, "
                        + column_lng + " real default null, "
                        + column_idleMode + " integer default null, "
                        + column_interactive + " integer default null, "
                        + column_scrstate + " integer default null, "
                        + column_lock_screen_allow_notifs+ " integer default null, "
                        + column_notification_flags + " integer default null ) "
        );

        /*
        private String column_idleMode = "idle";
    private String column_interactive = "interactive";
    private String column_scrstate = "screen_state";
    private String column_notification_flags = "flags";
         */

        db.execSQL(" create table " + ringer_table_name + "( "
                + column_userid + " integer default null, "
                + column_id + " integer primary key autoincrement, "
                + column_timestamp + " integer default null, "
                + column_RingerMode + " integer default null) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            setBackupDBname();
            String dbpath = exportDatabase();
            UploadDataTask task = new UploadDataTask(ctx, null);
            task.execute(dbpath);
        //changes in the database
        //db.execSQL(" drop table if exists " + notif_table_name);
        //db.execSQL(" drop table if exists " + ringer_table_name);
        //onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }


    public void insertData(CapturedNotification cn) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentValues.put(column_userid,sp.getString("username", "none"));
        contentValues.put(column_nid, cn.nid);
        contentValues.put(column_priority, cn.priority);
        contentValues.put(column_packageName, cn.packageName);
        contentValues.put(column_timePosted, cn.timePosted);
        contentValues.put(column_sound, cn. had_sound);
        contentValues.put(column_defaultSound, cn.has_default_sound);
        contentValues.put(column_LED, cn.led );
        contentValues.put(column_defaultLED, cn.has_default_lights);
        contentValues.put(column_vibrationPattern, cn.vibration );
        contentValues.put(column_defaultVibration, cn.has_default_vibe);
        contentValues.put(column_RingerMode, cn.ringerMode);
        contentValues.put(column_idleMode, cn.device_idle);
        contentValues.put(column_interactive, cn.screen_interactive);
        contentValues.put(column_scrstate, cn.screen_mode);
        contentValues.put(column_notification_flags, cn.flags);
        contentValues.put(column_lock_screen_allow_notifs, cn.lock_screen_notifs);


        Log.i(tag, cn.nid +
                "," + cn.packageName + "," + cn.ringerMode);
        db.insert(notif_table_name, null, contentValues);
        db.close();
    }

    public void insertRingerChangeData(int newmode)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentValues.put(column_userid,sp.getString("username", "none"));
        contentValues.put(column_RingerMode, newmode);
        contentValues.put(column_timestamp, System.currentTimeMillis()/1000);
        Log.i(tag, "Changed ringer Mode to "+newmode);
        db.insert(ringer_table_name, null, contentValues);
        db.close();
    }


    public void updateRemoveTime(StatusBarNotification sn, long removetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[2];
        args[0] = String.valueOf(sn.getId());
        args[1] = String.valueOf(sn.getPostTime() / 1000);
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_timeRemoved, removetime);
        int affrows =
                db.update(notif_table_name, contentValues, column_nid + "=?" + " AND " + column_timePosted + " = ?", args);
        Log.i(tag, "updated " + affrows + " rows");
        db.close();
    }

    public void updatePlace(int id, long posttime, PlaceLikelihood pll) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[2];
        args[0] = String.valueOf(id);
        args[1] = String.valueOf(posttime / 1000);
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_PlaceName, pll.getPlace().getName().toString());
        contentValues.put(column_PlaceCategories, intListToString(pll.getPlace().getPlaceTypes()));
        contentValues.put(column_PlaceId, pll.getPlace().getId());
        contentValues.put(column_PlaceConfidence, pll.getLikelihood());
        contentValues.put(column_lat, pll.getPlace().getLatLng().latitude);
        contentValues.put(column_lng, pll.getPlace().getLatLng().longitude);
        int affrows =
                db.update(notif_table_name, contentValues, column_nid + "=?" + " AND " + column_timePosted + " = ?", args);
        Log.i(tag, "updated " + affrows + " rows");
        db.close();
    }


    public String exportDatabase() {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + myapppkg + "//databases//notification.db";
                String newFolder = "NotifyMe";
                File destfolder = new File(sd, newFolder);

                if (!destfolder.exists())
                    destfolder.mkdirs();

                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(destfolder, backupDBname);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    Log.i(tag, "written to " + backupDB.getAbsolutePath());
                    return backupDB.getAbsolutePath();
                } else
                    Log.i(tag, "currentdb does not exist "+currentDB.getAbsolutePath());
            } else
                Log.i(tag, "can't write to sd");
        }
        catch (SecurityException e)
        {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getNotifications()
    {
        String notifications = "";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(notif_table_name, null,null,null,null,null,column_timePosted+" DESC","10");
        while(c.moveToNext()) {
            String notif = c.getString(
                    c.getColumnIndexOrThrow(column_packageName));
            //Log.i(tag, notif);
            notifications+=notif+"\n";
        }
        c.close();
        db.close();
        return notifications;
    }

    public String intListToString(List<Integer> l)
    {
        String cats="";
        for (int x : l)
        {
            cats+=x+",";
        }
        return cats.substring(0,cats.length()-1);
    }

    public void clearDB()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(notif_table_name, null, null);
        db.delete(ringer_table_name, null, null);
        db.close();
    }

}