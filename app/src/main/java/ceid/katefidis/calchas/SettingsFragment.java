package ceid.katefidis.calchas;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsFragment extends PreferenceFragment {

        public SettingsFragment() {}

        @SuppressWarnings("deprecation")
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);


            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference);

            SwitchPreference darkmodeSwitch = (SwitchPreference) findPreference("DarkMode");

            if (darkmodeSwitch != null) {
                darkmodeSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference arg0, Object isDarkModeOnObject) {
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
            }

            SwitchPreference socialSwitch = (SwitchPreference) findPreference("socialseek");

            if (socialSwitch != null) {
                socialSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference arg0, Object isSocialOnObject) {
                        getActivity().finish();
                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);
                        return true;
                    }
                });
            }
        }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
            //hide the search menu item on this fragment
            MenuItem searchItem = menu.findItem(R.id.menu_settings);
            searchItem.setVisible(false);

    }

}