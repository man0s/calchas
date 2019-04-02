package ceid.katefidis.calchas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {
    final Fragment homeFragment = new HomeFragment();
    final FragmentManager fm = getFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_contacts:
                    fm.beginTransaction()
                            .replace(R.id.main_container, new HomeFragment(), "home")
                            .addToBackStack(null)
                            .commit();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people")));
                    return false;
                case R.id.navigation_callLog:
                    fm.beginTransaction()
                            .replace(R.id.main_container, new HomeFragment(), "home")
                            .addToBackStack(null)
                            .commit();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls")));
                    return false;
                case R.id.navigation_settings:
                    fm.beginTransaction()
                            .replace(R.id.main_container, new SettingsFragment(), "settings")
                            .addToBackStack(null)
                            .commit();
                    return true;
            }
            return false;
        }
    };

    @Override
    public void onBackPressed() {
        BottomNavigationView mBottomNavigationView = findViewById(R.id.navigation);
        if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_home) {
            super.onBackPressed();
        } else {
            mBottomNavigationView.getMenu().findItem(R.id.navigation_settings).setChecked(false);
            mBottomNavigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
            fm.popBackStack();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        //set home tab as default navigation item
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //fm.beginTransaction().add(R.id.main_container, settingsFragment, "2").hide(settingsFragment).commit();
        fm.beginTransaction().add(R.id.main_container, homeFragment, "home").commit();

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_settings);
        SearchView searchView = (SearchView) searchItem.getActionView();
        //these flags together with the search view layout expand the search view in the landscape mode
        searchView.setQueryHint(getString(R.string.title_search_contacts));
        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (item.getItemId() == R.id.menu_settings) {
            //expand the search view when entering the activity(optional)
            item.expandActionView();



            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}


