package com.ikota.imagesns.ui;

import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListAdapter;
import android.widget.Toast;

import com.ikota.imagesns.R;


/**
 * Created by kota on 2015/04/18.
 * Settings screen
 */
public class SettingsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // set Toolbar as ActionBar
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);
        Drawable icon = getResources().getDrawable(R.drawable.ic_back);
        int tint = getResources().getColor(R.color.actionbtn_color);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        icon.setColorFilter(tint, mode);
        toolbar.setNavigationIcon(icon);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(
                    R.anim.fade_in_depth,
                    R.anim.slide_out_to_right
            );
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(
                R.anim.fade_in_depth,
                R.anim.slide_out_to_right
        );
    }



    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            // below code is only for the first time when user visits SettingsActivity.
            // Because at first, settings preference has no data about user but default preference has it.
            // So move its data to settings preference.
            SharedPreferences.Editor editor = getPreferenceManager().getSharedPreferences().edit();
            editor.putString("pref_name", Util.getUserName(getActivity())).apply();
        }

        @Override
        public void onResume() {
            super.onResume();
            reloadSummary();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(listener);
        }

        private void reloadSummary(){
            ListAdapter adapter = getPreferenceScreen().getRootAdapter();
            for (int i=0;i<adapter.getCount();i++){
                Object item = adapter.getItem(i);
                if (item instanceof EditTextPreference){
                    EditTextPreference preference = (EditTextPreference) item;
                    preference.setSummary(preference.getText() == null ? "" : preference.getText());
                }
            }
        }

        private SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                        if(key.equals("pref_name")) {
                            reloadSummary();
                            String new_name = sharedPreferences.getString(key, "");
                            Log.i("SettingsActivity", "name updated to " + new_name);
                            // todo not tested because of timeout error
                            // send update to server and save to locale
                            Util.saveUserName(getActivity(), new_name);
                            showToast(R.string.update_success);
//                        ApiCaller.getInstance().updateUserName(getActivity(), new_name, new ApiCaller.ApiListener() {
//                            @Override
//                            public void onPostExecute(String response) {
//                                showToast(R.string.update_success);
//                            }
//                            @Override
//                            public void onErrorListener(VolleyError error) {
//                                showToast(R.string.network_problem_message);
//                            }
//                        });
                        }

                    }
                };

        private void showToast(int str_id) {
            if(isAdded()) Toast.makeText(getActivity(), getResources().getString(str_id), Toast.LENGTH_SHORT).show();
        }
    }
}
