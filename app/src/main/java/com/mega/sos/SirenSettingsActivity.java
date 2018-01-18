package com.mega.sos;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;

public class SirenSettingsActivity  extends  AppCompatPreferenceActivity
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_SIREN_SOUND = "siren_sound";
    private static final String KEY_SIREN_VOLUME = "ring_volume";
    private ListPreference mSirenSoundPre;
    private SeekBarPreferenceVolume mVolumepre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings_siren);

        mSirenSoundPre = (ListPreference) getPreferenceScreen().findPreference(KEY_SIREN_SOUND);
        mSirenSoundPre.setOnPreferenceChangeListener(this);
        String stringValue = mSirenSoundPre.getSharedPreferences().getString(KEY_SIREN_SOUND, "0");
        int index = mSirenSoundPre.findIndexOfValue(stringValue);
        mSirenSoundPre.setSummary(
                index >= 0
                        ? mSirenSoundPre.getEntries()[index]
                        : null);

        mVolumepre = (SeekBarPreferenceVolume) getPreferenceScreen().findPreference(KEY_SIREN_VOLUME);
        int value = mVolumepre.getSharedPreferences().getInt(KEY_SIREN_VOLUME, 50);
        mVolumepre.setProgress(value);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)
        {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if (KEY_SIREN_SOUND.equals(key)) {
            String stringValue = newValue.toString();
            ListPreference listPreference = (ListPreference) preference;
            int index = listPreference.findIndexOfValue(stringValue);

            // Set the summary to reflect the new value.
            ((ListPreference) preference).setValue(stringValue);
            preference.setSummary(
                    index >= 0
                            ? listPreference.getEntries()[index]
                            : null);
        }

        return true;
    }

}
