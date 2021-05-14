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
import android.widget.Toast;

//import com.google.android.gms.location.places.PlaceLikelihood;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.List;

public class MyDBHelper extends SQLiteOpenHelper {

    private static final String database_name = "notifyme.db";

    private String notif_table_name = "notification_table";
    private String ringer_table_name = "ringer_table";
    private String questionnaire_table_name = "questionnaire_table";

    private static final int database_version = 1;

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
    private String column_PlaceName = "PlaceName";
    private String column_PlaceId = "PlaceId";
    private String column_PlaceCategories = "PlaceCategories";
    private String column_PlaceConfidence = "place_confidence";
    private String column_lat = "lat";
    private String column_lng = "lng";
    private String column_idleMode = "idle";
    private String column_interactive = "interactive";
    private String column_scrstate = "screen_state";
    private String column_notification_flags = "flags";
    private String column_lock_screen_allow_notifs = "lock_scr_notifs";
    private String column_ringer_event_id = "ringer_event_id";
    private String column_detected_activities = "Detected_activities";
    private String column_user_speed = "User_Speed";
    private String column_latitude = "Latitude";
    private String column_longitude = "Longitude";
    private String column_driving = "Driving";

    private String backupDBname;
    private String deviceID;

    private String column_timestamp = "eventtime";
    private String column_RingerMode = "ringerMode";

    private String column_handling = "handling";
    private String column_timehandled = "timehandled";
    private String column_location = "location";
    private String column_location_type = "location_type";
    private String column_social = "social";
    private String column_activity_type = "activity_type";
    private String column_activity = "activity";

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
                + column_notification_flags + " integer default null, "
                + column_detected_activities + " text, "
                + column_user_speed + " real default null, "
                + column_latitude + " real default null, "
                + column_longitude + " real default null, "
                + column_driving + " integer default null) "
        );

        db.execSQL(" create table " + ringer_table_name + "( "
                + column_userid + " integer default null, "
                + column_id + " integer primary key autoincrement, "
                + column_timestamp + " integer default null, "
                + column_RingerMode + " integer default null) "
        );

        db.execSQL(" create table " + questionnaire_table_name + "( "
                + column_userid + " integer default null, "
                + column_id + " integer primary key autoincrement, "
                + column_nid + " integer default null, "
                + column_ringer_event_id + " integer default null, "
                + column_timePosted + " integer default null, "
                + column_handling + " integer default null, "
                + column_timehandled + " integer default null, "
                + column_RingerMode + " integer default null, "
                + column_location + " integer default null, "
                + column_location_type + " integer default null, "
                + column_social + " text default null, "
                + column_activity_type + " integer default null, "
                + column_activity + " integer default null ) "
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        setBackupDBname();
        String dbpath = exportDatabase();
        UploadDataTask task = new UploadDataTask(ctx, null);
        task.execute(dbpath);
        //changes in the database
        db.execSQL(" drop table if exists " + notif_table_name);
        db.execSQL(" drop table if exists " + ringer_table_name);
        db.execSQL(" drop table if exists " + questionnaire_table_name);
        onCreate(db);
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
        contentValues.put(column_detected_activities, cn.activities);
        contentValues.put(column_user_speed, cn.speed);
        contentValues.put(column_latitude, cn.lat);
        contentValues.put(column_longitude, cn.lon);

        Log.i(tag, cn.nid +
                "," + cn.packageName + "," + cn.ringerMode);
        db.insert(notif_table_name, null, contentValues);
        db.close();
    }

    public void updateRemoveTime(StatusBarNotification sn, long removetime) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[2];
        args[0] = String.valueOf(sn.getId());
        args[1] = String.valueOf(sn.getPostTime());
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_timeRemoved, removetime);
        Log.i(tag, "updated " + removetime);
        int affrows =
                db.update(notif_table_name, contentValues, column_nid + "=?" + " AND " + column_timePosted + " = ?", args);
        Log.i(tag, "updated " + affrows + " rows");
        db.close();
    }

    public void updatePlace(int id, long posttime, String placeName, double placeLikelihood, String placeType, double lat, double lon, String placeID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[2];
        args[0] = String.valueOf(id);
        args[1] = String.valueOf(posttime);
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_PlaceName, placeName);
        contentValues.put(column_PlaceCategories, placeType);
        contentValues.put(column_PlaceId, placeID);
        contentValues.put(column_PlaceConfidence, placeLikelihood);
        contentValues.put(column_lat, lat);
        contentValues.put(column_lng, lon);
        Log.i("Placesz", placeName + placeLikelihood + placeType + placeID + lat + lon);
        int affrows =
                db.update(notif_table_name, contentValues, column_nid + "=?" + " AND " + column_timePosted + " = ?", args);
        Log.i(tag, "updatedPlaces " + affrows + " rows");
        db.close();
    }

    public void updateDriving(int id, long timePosted, int choice) {

        SQLiteDatabase db = this.getWritableDatabase();
        String[] args = new String[2];
        args[0] = String.valueOf(id);
        args[1] = String.valueOf(timePosted);
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_driving, choice);
        Log.i("Driving", "User action:" + choice);
        int affrows =
                db.update(notif_table_name, contentValues, column_nid + "=?" + " AND " + column_timePosted + " = ?", args);
        Log.i(tag, "updatedPlaces " + affrows + " rows");
        db.close();
    }

    public long insertRingerChangeData(int newmode)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentValues.put(column_userid,sp.getString("username", "none"));
        contentValues.put(column_RingerMode, newmode);
        contentValues.put(column_timestamp, System.currentTimeMillis()/1000);
        long id = db.insert(ringer_table_name, null, contentValues);
        Log.i(tag, "Inserted ringer table data, mode to "+newmode+", recordid = "+id);
        db.close();
        return id;
    }

    public long insertQuestionnaireNotifData(int questId, long timeposted, long latesteventid)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        contentValues.put(column_userid,sp.getString("username", "none"));
        contentValues.put(column_nid,questId);
        contentValues.put(column_ringer_event_id, latesteventid);
        contentValues.put(column_timePosted,timeposted);
        long insertid = db.insert(questionnaire_table_name, null, contentValues);
        Log.i(tag, "Questionnaire data created for ringer event id "+latesteventid+", recordid="+insertid);
        db.close();
        return insertid;
    }

    public void updateQuestionnaireNotifRemoved(long id, int handle, long timehandled){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_handling,handle);
        contentValues.put(column_timehandled,timehandled);
        Log.i(tag, "Questionnaire's notification removed");
        int affrows =
                db.update(questionnaire_table_name, contentValues, column_id + "="+id, null);
        Log.i(tag, "updated on id ="+id+", affected " + affrows + " rows");
        db.close();
    }

    public void updateQuestionnaireCanceled(long id, int handle,long timehandled){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_handling,handle);
        contentValues.put(column_timehandled,timehandled);
        Log.i(tag, "Questionnaire's submission canceled");
        int affrows =
                db.update(questionnaire_table_name, contentValues, column_id + "="+id, null);
        Log.i(tag, "updated on id ="+id+", affected " + affrows + " rows");
        db.close();
    }

    public void updateQuestionnaireAnsweredData(long id, int handle, long timehandled, int currentmode, int currlocation, int currlocation_type, String currsocial, int curractivity_type, int curractivity){
        SQLiteDatabase db = this.getWritableDatabase();
        //SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        //String[] args = new String[2];
        //args[0] = String.valueOf(id);
        //args[1] = String.valueOf(posttime/1000);
        ContentValues contentValues = new ContentValues();
        contentValues.put(column_handling, handle);
        contentValues.put(column_timehandled, timehandled);
        contentValues.put(column_RingerMode, currentmode);
        contentValues.put(column_location, currlocation );
        contentValues.put(column_location_type, currlocation_type);
        contentValues.put(column_social, currsocial);
        contentValues.put(column_activity_type, curractivity_type);
        contentValues.put(column_activity, curractivity);
        Log.i(tag, "Questionnaire answered" );
        int affrows =
                db.update(questionnaire_table_name, contentValues, column_id + "="+id, null);
        Log.i(tag, "updated on id ="+id+", affected " + affrows + " rows");
        db.close();
    }

    public String exportDatabase() {

        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "//data//" + myapppkg + "//databases//notifyme.db";
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
                    Toast.makeText(ctx, "Exported db "+ backupDB.getAbsolutePath(),Toast.LENGTH_SHORT).show();
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
        db.delete(questionnaire_table_name, null, null);
        db.close();
    }
}