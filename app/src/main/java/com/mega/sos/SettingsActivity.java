package com.mega.sos;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by lixh on 2017/12/8.
 */

public class SettingsActivity  extends  AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_SENDING_TIME = "sending_time";
    private static final String KEY_EMERGENCY_SOS = "emergency_sos";
    private static final String KEY_SIREN = "Siren";
    private ListPreference mSendingtimePre;
    private Preference mEmergencysosPre;
    private Preference mSirenPre;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_setings);

        mSendingtimePre = (ListPreference) getPreferenceScreen().findPreference(KEY_SENDING_TIME);
        mEmergencysosPre = (Preference) getPreferenceScreen().findPreference(KEY_EMERGENCY_SOS);
        mSirenPre = (Preference) getPreferenceScreen().findPreference(KEY_SIREN);

        mSendingtimePre.setOnPreferenceChangeListener(this);
        mEmergencysosPre.setOnPreferenceChangeListener(this);
        mSirenPre.setOnPreferenceChangeListener(this);



       // String hotspotMode = mSendingtimePre.getValue();
        //mSendingtimePre.setValue(hotspotMode);

        int timeInterval = Utils.getSendSMSTimeInterval(this);
        mSendingtimePre.setValue(timeInterval+"");
        mSendingtimePre.setSummary(mSendingtimePre.getEntry());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (KEY_SENDING_TIME.equals(key)) {
            String stringValue = newValue.toString();
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
            Utils.putSendSMSTimeInterval(this,Integer.parseInt(stringValue));
      /*  }else if(KEY_EMERGENCY_SOS.equals(key)){
            Log.v("lixh","-------emergency_sos---------->>>>");*/
        } else if (KEY_SIREN.equals(key)) {
            Log.v("lixh","-------emergency_sos---------->>>>");

        }
        return true;
    }

}
