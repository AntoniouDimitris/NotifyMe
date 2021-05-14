package com.notify.notifyme;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

public class RingerChangedActivity extends AppCompatActivity {

    private RadioButton publicplace;
    private RadioButton privateplace;
    private RadioGroup placegroup;
    private Spinner locations, activity_type, activity;
    private Button social, submit;
    private Button cancel;
    private SharedPreferences sp;

    private String[] Sociallist;
    private boolean[] SocialcheckedItems;
    ArrayList<Integer> mSocialItems = new ArrayList<>();
    private TextView SocialItemSelected;
    private TextView OngoingActivityquest;

    public int radiobtn = -1;
    public int location_no = -1;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ringer_changed);


        final ArrayList<String> locationlist =
                new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.locations_array)));

        //final ArrayList<String> Sociallist = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.social_array)));
        Sociallist = getResources().getStringArray(R.array.social_array);
        SocialcheckedItems = new boolean[Sociallist.length];
        //initialize with false
        Arrays.fill(SocialcheckedItems, Boolean.FALSE);

        final ArrayList<String> activitylist =
                new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.activity_array)));


        final ArrayAdapter<String> adapterLocations = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, locationlist);

        final ArrayAdapter<String> adapterActivities = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, activitylist);


        publicplace = (RadioButton) findViewById(R.id.radioButton_public);
        privateplace = (RadioButton) findViewById(R.id.radioButton_private);

        placegroup = (RadioGroup) findViewById(R.id.radio_group);
        locations = (Spinner) findViewById(R.id.spinner_location);
        locations.setVisibility(View.GONE);

        social = (Button) findViewById(R.id.social_button);
        social.setVisibility(View.GONE);

        SocialItemSelected = (TextView) findViewById(R.id.textView_social);
        SocialItemSelected.setVisibility(View.GONE);

        OngoingActivityquest = (TextView) findViewById(R.id.textView_ongoing_activity);
        OngoingActivityquest.setVisibility(View.GONE);

        activity_type = (Spinner) findViewById(R.id.spinner_activity_type);
        activity_type.setVisibility(View.GONE);

        activity = (Spinner) findViewById(R.id.spinner_activity);
        activity.setVisibility(View.GONE);

        submit = (Button) findViewById(R.id.button2);
        submit.setEnabled(false);

        cancel = (Button) findViewById(R.id.button3);

        placegroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.i("RADIO BUTTON", "hit "+i);

                if(i==R.id.radioButton_private) {
                    radiobtn = 0;
                    Log.i("RADIO BUTTON", "Private location :"+radiobtn);
                    adapterLocations.clear();
                    adapterLocations.addAll(getResources().getStringArray(R.array.locations_private_array));
                }
                else
                {
                    radiobtn = 1;
                    Log.i("RADIO BUTTON", "Public location :"+radiobtn);
                    adapterLocations.clear();
                    adapterLocations.addAll(getResources().getStringArray(R.array.locations_public_array));
                }
                adapterLocations.notifyDataSetChanged();
                adapterLocations.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                //Show locations spinner
                locations.setVisibility(View.VISIBLE);
                // Apply the adapter to the spinner
                locations.setAdapter(adapterLocations);
                locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        location_no=i;
                        Log.i("LOCATION", "RECORDED "+location_no);
                        if (location_no>0){
                            social.setVisibility(View.VISIBLE);
                        }
                        social.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AlertDialog.Builder mBuilder = new AlertDialog.Builder(RingerChangedActivity.this);
                                mBuilder.setTitle(R.string.social_label);
                                mBuilder.setMultiChoiceItems(Sociallist, SocialcheckedItems,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
                                        Log.i("SOCIAL", "selected " + position);

                                        if (isChecked){
                                            if (!mSocialItems.contains(position)){
                                                mSocialItems.add(position);
                                            }else {
                                                mSocialItems.remove(Integer.valueOf(position));
                                            }
                                            if (mSocialItems.size()>1 && mSocialItems.contains(0)){
                                                mSocialItems.clear();
                                                AlertDialog.Builder
                                                        conBuilder = new AlertDialog.Builder(RingerChangedActivity.this);
                                                conBuilder.setTitle(R.string.alertdialog_error_message);
                                                conBuilder.setNegativeButton(R.string.dismiss_btn_label, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        dialogInterface.dismiss();
                                                    }
                                                });
                                                AlertDialog conDialog = conBuilder.create();
                                                conDialog.show();
                                                Arrays.fill(SocialcheckedItems, Boolean.FALSE);

                                                SocialItemSelected.setText("");
                                                dialogInterface.dismiss();
                                            }
                                        }else {
                                            if (mSocialItems.contains(position)){
                                                mSocialItems.remove(Integer.valueOf(position));
                                            }
                                        }
                                    }
                                });
                                mBuilder.setCancelable(false);
                                mBuilder.setPositiveButton(R.string.ok_label, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        String item = "";
                                        for (int i = 0; i < mSocialItems.size(); i++) {
                                            item = item + Sociallist[mSocialItems.get(i)];
                                            if (i != mSocialItems.size() - 1) {
                                                item = item + ", ";
                                            }
                                        }
                                        Log.i("SOCIAL", "submitted "+ item);
                                        SocialItemSelected.setText(item);
                                        if( SocialItemSelected.getText().length()==0){
                                            AlertDialog.Builder
                                                    emptyBuilder = new AlertDialog.Builder(RingerChangedActivity.this);
                                            emptyBuilder.setTitle(R.string.emptyalert_message);
                                            emptyBuilder.setNegativeButton(R.string.dismiss_btn_label, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dialogInterface.dismiss();
                                                }
                                            });
                                            AlertDialog conDialog = emptyBuilder.create();
                                            conDialog.show();
                                        }else {
                                            SocialItemSelected.setVisibility(View.VISIBLE);
                                            OngoingActivityquest.setVisibility(View.VISIBLE);
                                            activity_type.setVisibility(View.VISIBLE);
                                            activity_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                @Override
                                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                                    Log.i("ACTIVITY TYPE", "selected "+i);
                                                    adapterActivities.clear();
                                                    switch (i)
                                                    {
                                                        case 2:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_exercise));
                                                            break;
                                                        case 3:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_leisure));
                                                            break;
                                                        case 4:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_relax));
                                                            break;
                                                        case 5:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_socialevent));
                                                            break;
                                                        case 6:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_study));
                                                            break;
                                                        case 7:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_travel));
                                                            break;
                                                        case 8:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_work));
                                                            break;
                                                        case 9:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_other));
                                                            break;
                                                        default:
                                                            adapterActivities.addAll(getResources()
                                                                    .getStringArray(R.array.activity_array));
                                                            break;
                                                    }
                                                    adapterActivities.notifyDataSetChanged();
                                                    adapterActivities
                                                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                                    //Show activity spinner
                                                    if (i>0){
                                                        activity.setVisibility(View.VISIBLE);
                                                    }
                                                    // Apply the adapter to the spinner
                                                    activity.setAdapter(adapterActivities);
                                                    activity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
                                                        @Override
                                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l){
                                                            Log.i("ACTIVITY", "selected "+i);
                                                            if (i>0){
                                                                submit.setEnabled(true);
                                                            }
                                                        }
                                                        @Override
                                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                                            //submit.setEnabled(false);
                                                        }
                                                    });
                                                }
                                                @Override
                                                public void onNothingSelected(AdapterView<?> adapterView) {
                                                    //submit.setEnabled(false);
                                                }
                                            });
                                        }
                                    }
                                });
                                mBuilder.setNegativeButton(R.string.dismiss_label, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        for(int j = SocialcheckedItems.length-1 ; j >= 0; j--) {
                                            SocialcheckedItems[j] = false;
                                        }
                                        dialogInterface.dismiss();
                                    }
                                });
                                mBuilder.setNeutralButton(R.string.clear_all_label, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        for (int i = 0; i< SocialcheckedItems.length; i++) {
                                            SocialcheckedItems[i] = false;
                                        }
                                        mSocialItems.clear();
                                        SocialItemSelected.setText("");
                                    }
                                });
                                AlertDialog mDialog = mBuilder.create();
                                mDialog.show();
                            }
                        });
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        //submit.setEnabled(false);
                    }
                });
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                int radio = -1;
                if (placegroup.getCheckedRadioButtonId()==R.id.radioButton_private){
                    radio = 0;
                }else if (placegroup.getCheckedRadioButtonId()==R.id.radioButton_public){
                    radio = 1;
                }
                */

                sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                //SharedPreferences.Editor e = sp.edit();

                MyDBHelper db = new MyDBHelper(getApplicationContext());
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                db.updateQuestionnaireAnsweredData(sp.getLong("lastinsertid",-1),2,
                        System.currentTimeMillis()/1000, am.getRingerMode(), radiobtn,
                        locations.getSelectedItemPosition(), SocialItemSelected.getText().toString(),
                        activity_type.getSelectedItemPosition(),
                        activity.getSelectedItemPosition());

                //e.putLong("lastform", System.currentTimeMillis());
                //e.commit();

                Log.i("ACTIVITIES", "updated form submit time: "+ System.currentTimeMillis());

                finish();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MyDBHelper db = new MyDBHelper(getApplicationContext());
                sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                db.updateQuestionnaireCanceled(sp.getLong("lastinsertid",-1),1,
                        System.currentTimeMillis()/1000);

                //go back to MainActivity
                Intent intent = new Intent(RingerChangedActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
