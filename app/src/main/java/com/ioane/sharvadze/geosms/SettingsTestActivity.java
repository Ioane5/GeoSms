package com.ioane.sharvadze.geosms;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.Map;


public class SettingsTestActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_test);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener{

        private static final String TAG = SettingsFragment.class.getSimpleName();

        public SettingsFragment() {}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            MyPreferencesManager.getWebSmsPreferences(getActivity().getBaseContext()).
                    registerOnSharedPreferenceChangeListener(this);

            getPreferenceManager().setSharedPreferencesMode(MODE_PRIVATE);
            SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

            onSharedPreferenceChanged(preferences,MyPreferencesManager.WEBSMS_USERNAME);
            onSharedPreferenceChanged(preferences,MyPreferencesManager.WEBSMS_NAME);
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            if (key.equals(MyPreferencesManager.WEBSMS_USERNAME)) {
                pref.setSummary(sharedPreferences.getString(key, ""));
            }else if(key.equals(MyPreferencesManager.WEBSMS_NAME)){
                ListPreference listPreference = (ListPreference)pref;
                int index = listPreference.findIndexOfValue(sharedPreferences.getString(key,"-1"));

                pref.setSummary(index >= 0
                        ? listPreference.getEntries()[index]
                        : null);
            }
        }
    }
}
