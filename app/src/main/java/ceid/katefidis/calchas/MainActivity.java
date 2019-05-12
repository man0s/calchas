package ceid.katefidis.calchas;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;


class calllogrecord
{
    String number;
    long date;
    String CachedName;
    boolean isContact;

    public calllogrecord(String num, long mydate, String CachedName, boolean isContact)
    {
        this.number = num;
        this.date = mydate;
        this.CachedName = CachedName;
        this.isContact = isContact;
    }

}

class Protasi
{
    String number;
    String name;
    String contactID;
    double score;
    double scoref;
    double scorer;
    boolean isContact;
    Bitmap photo;
    long date;
    boolean suggested;
    String network;

    public Protasi(String num, String name,double scoref, double scorer, double score, boolean isContact, String contactID)
    {
        this.number = num;
        this.score = score;
        this.name = name;
        this.isContact = isContact;
        this.contactID = contactID;
        this.scoref = scoref;
        this.scorer = scorer;
        this.suggested = false;
    }


}

class CompareProtaseis implements Comparator<Protasi>
{

    @Override
    public int compare(Protasi protasi1, Protasi protasi2)
    {
        if (protasi1.score >= protasi2.score)
            return -1;
        else
            return 1;
    }
}


//Deuteros custom comparator gia to alfavitiko
class SortAlphaProtaseis implements Comparator<Protasi>
{

    @Override
    public int compare(Protasi protasi1, Protasi protasi2)
    {
        return protasi1.name.compareToIgnoreCase(protasi2.name);
    }
}

//Tritos custom comparator gia to sort me vasi to date
class SortDateProtaseis implements Comparator<Protasi>
{

    @Override
    public int compare(Protasi protasi1, Protasi protasi2)
    {
        if (protasi1.date >= protasi2.date)
            return -1;
        else
            return 1;
    }
}

//Tritos custom comparator gia to sort me vasi to date
class SortDateCallLogRecord implements Comparator<calllogrecord>
{

    @Override
    public int compare(calllogrecord calllog1, calllogrecord calllog2)
    {
        if (calllog1.date >= calllog2.date)
            return -1;
        else
            return 1;
    }
}

public class MainActivity extends AppCompatActivity {
    //Dilwsi ton static metavlitwn
    //DEVELOPER

    double wf = 0.5F;
    double wr = 1.0 - wf;

    //DEVELOPER OPTIONS
    ArrayList<calllogrecord> subcalllog = new ArrayList<calllogrecord>();
    ArrayList<Protasi> finalprotaseis = new ArrayList<Protasi>();
    MobileArrayAdapter arrayAdapter;
//    //	Tracker tracker;
//    Long start_time;
//    Long end_time;
    //HashMap<String, Integer> countryCodes;
    private NotificationDBHelper db;
    private SQLiteDatabase sdb;


    final Fragment homeFragment = new HomeFragment();
    final FragmentManager fm = getFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_contacts:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people")));
                    return false;
                case R.id.navigation_callLog:
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls")));
                    return false;
                case R.id.navigation_settings:
                    fm.beginTransaction()
                            .replace(R.id.main_container, new SettingsFragment(), "settings")
                            .disallowAddToBackStack()
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
            Toast.makeText(MainActivity.this, "Press back again to leave",
                    Toast.LENGTH_LONG).show();
            mBottomNavigationView.getMenu().findItem(R.id.navigation_settings).setChecked(false);
            mBottomNavigationView.getMenu().findItem(R.id.navigation_home).setChecked(true);
//            fm.popBackStack();
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private SuggestionsChangeBroadcastReceiver suggestionsChangeBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean DarkMode = settings.getBoolean("DarkMode", false);
        boolean socialSeek = settings.getBoolean("socialseek", false);

        if(DarkMode) {
            setTheme(R.style.DarkTheme);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        if(!Permissions.Check_PERMISSIONS(MainActivity.this))
        {
            //if not permisson granted so request permisson with request code
            Permissions.Request_PERMISSIONS(MainActivity.this,1);
        }
        setContentView(R.layout.activity_main);
        setupWindowAnimations();


        if(socialSeek) {
            // If the user did not turn the notification listener service on we prompt him to do so
            if (!isNotificationServiceEnabled()) {
                enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog();
                enableNotificationListenerAlertDialog.show();
            }
        }

        // Finally we register a receiver to tell the MainActivity when a notification has been received
        suggestionsChangeBroadcastReceiver = new SuggestionsChangeBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ceid.katefidis.calchas");
        registerReceiver(suggestionsChangeBroadcastReceiver, intentFilter);


        BottomNavigationView navigation = findViewById(R.id.navigation);
        //set home tab as default navigation item
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //fm.beginTransaction().add(R.id.main_container, settingsFragment, "2").hide(settingsFragment).commit();
        fm.beginTransaction().add(R.id.main_container, homeFragment, "home").commit();

        Toolbar mTopToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mTopToolbar);


//        TextView tv = (TextView) findViewById(R.id.calchasLogo);
//        Typeface face = Typeface.createFromAsset(getAssets(),
//                "fonts/Lobster.ttf");
//        tv.setTypeface(face);

//		//setup tracking
//		GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
//		analytics.setDryRun(false);
//		tracker = analytics.newTracker(R.xml.tracker);
//		analytics.getLogger().setLogLevel(0);
//		tracker.set("&uid", Secure.getString(this.getContentResolver(),
//                Secure.ANDROID_ID));
//        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
//        countryCodes = new HashMap<String, Integer>();
//        for(int i=0;i<rl.length;i++)
//        {
//            String[] g=rl[i].split(",");
//            countryCodes.put(g[1], Integer.parseInt(g[0]));
//        }

        boolean firstRun = settings.getBoolean("firstRun", true);
        if ( firstRun )
        {

//            startActivityForResult(
//                    new Intent(this, ConsentActivity.class),
//                    1);

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(suggestionsChangeBroadcastReceiver);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = settings.edit();
        if(resultCode == RESULT_OK)
        {
            editor.putBoolean("resolve_numbers", true);
        }
        else
        {
            editor.putBoolean("resolve_numbers", false);
        }
        editor.putBoolean("firstRun", false);
        editor.apply();

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.search, menu);
        MenuItem searchItem = menu.findItem(R.id.menu_settings);
        SearchView searchView = (SearchView) searchItem.getActionView();
        //these flags together with the search view layout expand the search view in the landscape mode
        searchView.setQueryHint(getString(R.string.title_search_contacts));
        searchItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW
                | MenuItem.SHOW_AS_ACTION_ALWAYS);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            Integer oldquery = 0;
            @Override
            public boolean onQueryTextSubmit(String query) {
                // collapse the view ?
//                searchItem.collapseActionView();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (query.length() < oldquery)
                {
                    // We're deleting char so we need to reset the adapter data
                    arrayAdapter.resetData();
                }
                arrayAdapter.getFilter().filter(query);
                oldquery = query.length();
                return false;
            }
        });

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


//    @Override
//    protected void onPause()
//    {
//        end_time = System.currentTimeMillis();
//        //send the session duration
//        tracker.send(new HitBuilders.TimingBuilder()
//                .setCategory("Timings")
//                .setValue(end_time-start_time)
//                .setVariable("SessionDuration")
//                .setLabel("Session Duration")
//                .build());
//
//        //send the total number of outgoing calls
//        SharedPreferences preferences = this.getSharedPreferences("MyPrefs", 0);
//        //PreferenceManager.getDefaultSharedPreferences(this);
//        int calls = preferences.getInt("total_calls", 0);
//
//        Log.i("OnPause", "Sending total calls: "+calls);
//
//        tracker.send(
//                new HitBuilders.EventBuilder()
//                        .setCategory("OutgoingCalls")
//                        .setAction("Sum")
//                        .setLabel("TotalOutGoingCalls")
//                        .setValue(calls)
//                        .build());
//
//        Editor e = preferences.edit();
//        e.putInt("total_calls", 0);
//        e.commit();
//
//        super.onPause();
//
//        GoogleAnalytics.getInstance(this).dispatchLocalHits();
//    }
//
//	/*
//	@Override
//	protected void onStart()
//	{
//		GoogleAnalytics.getInstance(this).reportActivityStart(this);
//	}
//
//	@Override
//	protected void onStop()
//	{
//		GoogleAnalytics.getInstance(this).reportActivityStop(this);
//	}
//	*/

    @Override
    protected void onResume() {
        super.onResume();

//        //start new tracker session
//        tracker.send(new HitBuilders.AppViewBuilder()
//                .setNewSession()
//                .build());

        if (Permissions.Check_PERMISSIONS(MainActivity.this)) {
            //init database
            //StoreStatsSQLlite db = new StoreStatsSQLlite(this);

            //DEVELOPER Epeidi ta evgala exw gia to dump!!!
            subcalllog = new ArrayList<calllogrecord>();
            finalprotaseis = new ArrayList<Protasi>();

            //pernw apo ta user preferences tis antistoixes times
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String strNumProtaseis = preferences.getString("protaseis", "7");
            String selectedinterface = preferences.getString("inteface", "3");
            boolean showphoto = preferences.getBoolean("showphoto", true);
            boolean smsSeek = preferences.getBoolean("smsseek", true);
            boolean socialSeek = preferences.getBoolean("socialseek", false);
//          boolean showsearch = preferences.getBoolean("showsearch", true);

            //DEVELOPER
            wf = Double.parseDouble(preferences.getString("varos", "0.5"));
            wr = (1000.0F - 1000.0F * wf) / 1000.0F;

            //se periptwsi pou kapoios valei NULL stis epiloges na gyrizei sta default
            long freqWindow = 15L;
            String tempfreqWindow = preferences.getString("freqwindow", "15");
            if (tempfreqWindow.length() != 0)
                freqWindow = Long.parseLong(tempfreqWindow);

            long recenWindow = 12L;
            String temprecenWindow = preferences.getString("recwindow", "12");
            if (temprecenWindow.length() != 0)
                recenWindow = Long.parseLong(temprecenWindow);

            //O arithmos twn protasewn diavazetai apo ta preferences tou xristi
            int numProtaseis = Integer.parseInt(strNumProtaseis);

            ListView lista1 = (ListView) findViewById(R.id.list);

            //edw tha apothikeusw to call log gia to diastima pou me endiaferei
            //DEVELOPER OPTION TO EXW VGALEI EXE
            //ArrayList<calllogrecord> subcalllog = new ArrayList<calllogrecord>();
            //edw tha apothikeusw tis monadikes eggrafes tou call log
            HashSet<String> subcallunique = new HashSet<String>();
            //edw tha apothikeusw tis protaseis
            final ArrayList<Protasi> protaseis = new ArrayList<Protasi>();

            //Pairnw to call log gia oses meres thelw kai to vaze se ena array list me calllog records
            subcalllog = getCallLog(freqWindow, smsSeek, socialSeek);

            //Pairnw kai tis monadikes eggrafes mesa sto calllog
            subcallunique = getUniqueCallRecords(subcalllog);

            //Gia kathe monadiki eggrafi ypologizw tis metavlites pou thelw
            for (String uniqueCachedName : subcallunique) {
                //ypologismos Frequency mesa sto subcalllog
                double freqscore = calcFreq(subcalllog, uniqueCachedName);

                //ypologismos Recence mesa sto subcalllog
                double recenscore = calcRecenc(subcalllog, uniqueCachedName, recenWindow);

                //afou ta ypologisa ftiaxnw ena neo antikeimeno protasi kai to apothikeuw sto antistoixo array list
                //protasi prot = new protasi(uniqueCachedName, getContactName(uniqueCachedName), calcScore(freqscore, recenscore));
                Protasi prot = new Protasi("NoNumberYet", uniqueCachedName, freqscore, recenscore, calcScore(freqscore, recenscore), false, "");
                protaseis.add(prot);

            }

            //Thelw tis top N protaseis apo to ArrayList protaseis me vasi to megalitero score
            //sortarw me to custom comperator
            Collections.sort(protaseis, new CompareProtaseis());

            //Krataw mono tis N protes protaseis
            //protaseis.subList(0, numProtaseis-1);


            //DEVEOPER OPTION
            //final ArrayList<protasi> finalprotaseis = new ArrayList<protasi>();
            int i = 0;
            for (Protasi protasitemp : protaseis) {


                for (calllogrecord s1 : subcalllog) {
                    if (protasitemp.name.equals(s1.CachedName)) {
                        protasitemp.number = s1.number;
                        protasitemp.date = s1.date;
                        //Gia na kalypsw tin periptwsi pou to cachedname den exei ananewthei diavazw gia tis protaseis
                        //to kanoniko name apo tis epafes
                        //protasitemp.name = s1.CachedName;
                        protasitemp.name = getContactName(protasitemp.number);
                        protasitemp.contactID = getContactID(protasitemp.number);
                        protasitemp.suggested = true;

                        //An yparxei contactID simainei oti einai epafi
                        if (protasitemp.contactID.length() != 0)
                            protasitemp.isContact = true;
                        else
                            protasitemp.isContact = false;

                        //an o xristis exei epilexei na fainontai oi photos
                        if (showphoto && protasitemp.isContact)
                            protasitemp.photo = loadPhoto(Long.parseLong(protasitemp.contactID));
                        //To break SIMANTIKO wste an vrethei auto pou thelw sto subcalllog na mi xreiatei na diatrexw oli ti lista
                        break;
                    }
                }

                finalprotaseis.add(protasitemp);

                i++;
                //Thelw tis N kalyteres protaseis
                //To 9 isodynamei me to na deixei oles tis protaseis ara den thelw to break
                if (i > numProtaseis - 1 && numProtaseis != 9)
                    break;
            }


            ///////////////// ----------- INTERFACES ----------- /////////////////
            //Kanw sort tis final protaseis alphavitika

            //an o xristis exei epilexei to interface 2 h' 5 h' 6 pou exoun alfavitiki parousiasi twn epilogwn
            if (selectedinterface.equalsIgnoreCase("2") || selectedinterface.equalsIgnoreCase("5") || selectedinterface.equalsIgnoreCase("6"))
                Collections.sort(finalprotaseis, new SortAlphaProtaseis());


            //An o xristis exei epilexei to interface 3 h' 5 fortwn
            //katw apo tis protaseis kai to Call log me header tin fake kataxwrisi
            if (selectedinterface.equalsIgnoreCase("3") || selectedinterface.equalsIgnoreCase("5")) {
			/* An thelw na kripsw to koumpi tou call log
			ImageButton buttoncallloghide = (ImageButton)findViewById(R.id.imageButton2);
			buttoncallloghide.setVisibility(View.INVISIBLE);
			*/

                ArrayList<Protasi> mycalllog = new ArrayList<Protasi>();

                //Prosthetw stin ousia to header to Call Log
                //Vazontas mia fake "kataxwrisi" me arithmo tilefwnou -1 kai onoma Call Log
                //Des to MobileArrayAdapter.java
                //To score to vazw -3 gia tous seperators
                //kai -2 gia ta contacts tou contact list stin getAllContracts()

                //edw ftiaxnw to unique call log
                for (String uniquecallname : subcallunique) {
                    boolean flag = true;
                    //prin diatrexw to call log wste na parw ta stoixia tis monadikis epafis
                    //koitaw mipws yparxei mesa stis final protaseis
                    for (Protasi singleprotasi : finalprotaseis) {
                        if (uniquecallname.equals(singleprotasi.name) || uniquecallname.equals(singleprotasi.number)) {
                            flag = false;
                            break;
                        }
                    }

                    //an h egrafi to call log DEN yparxei stis protaseis
                    //ara to flag tha exei paraminei true
                    if (flag) {
                        for (calllogrecord singlecallrecord : subcalllog) {
                            if (singlecallrecord.CachedName.equals(uniquecallname)) {
                                Protasi mycalllogrecord = new Protasi(singlecallrecord.number, singlecallrecord.CachedName, 0.0, 0.0, 0.0, singlecallrecord.isContact, "");
                                mycalllogrecord.date = singlecallrecord.date;

                                //se periptwsi pou thenw na emfanizw kai tis photos sto unique call log
                                if (showphoto && mycalllogrecord.isContact) {
                                    try {
                                        mycalllogrecord.contactID = getContactID(mycalllogrecord.number);
                                        mycalllogrecord.photo = loadPhoto(Long.parseLong(mycalllogrecord.contactID));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                mycalllog.add(mycalllogrecord);
                                break;
                            }
                        }
                    }
                }
                //sortarw ta recent calls me xronologiki seira
                Collections.sort(mycalllog, new SortDateProtaseis());


                //meta to sort vazw ton seperator prwto sti lista
                Protasi mycalllogrecordnull = new Protasi("-1", "Recent Calls", 0.0, 0.0, -3.0, false, "");
                mycalllog.add(0, mycalllogrecordnull);

			/*
			for (calllogrecord s1 : subcalllog)
			{
				protasi mycalllogrecord = new protasi(s1.number, s1.CachedName, 0.0, 0.0, 0.0, s1.isContact, "");
				mycalllogrecord.date = s1.date;
				mycalllog.add(mycalllogrecord);
			}
			*/
                finalprotaseis.addAll(mycalllog);
            }

            if (selectedinterface.equalsIgnoreCase("4") || selectedinterface.equalsIgnoreCase("6")) {
                //arxika ftiaxnw ena arraylist me ola ta contact ids twn protasewn
                ArrayList<String> protaseisContactsIds = new ArrayList<String>();

                for (Protasi singleprotasi : finalprotaseis)
                    protaseisContactsIds.add(singleprotasi.contactID);

                ArrayList<Protasi> mycontactlist = getAllContactsWithoutProtaseis(showphoto, protaseisContactsIds);
                finalprotaseis.addAll(mycontactlist);
            }

//            //add network names to each protasi and if a protasi has no network, add it to the "resolve" list
//            StoreStatsSQLlite sqlHelper = new StoreStatsSQLlite(this);
//            ArrayList<String> numbersToResolve = new ArrayList<String>();
//            ArrayList<Protasi> protaseisToResolve = new ArrayList<Protasi>();
//            //check for network names
//
//            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//            String country = tm.getNetworkCountryIso();
//            if (country.length() < 2)
//                country = tm.getSimCountryIso(); //in case you can't get it from the 3G current network, revert to sim info
//
//            if (country == null || country == "") {
//                country = "GB";
//            }
//
//            Locale l = new Locale("en", country);
//            int countrycode = countryCodes.get(country.toUpperCase());
//            for (int x = 0; x < finalprotaseis.size(); x++) {
//                Protasi protasiTemp = finalprotaseis.get(x);
//                String nn = sqlHelper.getNetworkName(protasiTemp.number);
//
//                if (nn == null) {
//                    if (protasiTemp.number.length() < 12) //number is not in correct intl format
//                    {
//                        //check android version
//                        //if(android.os.Build.VERSION.SDK_INT<21)
//                        //{
//                        protasiTemp.number = "+" + countrycode + protasiTemp.number;
//                        Editable e = new SpannableStringBuilder(protasiTemp.number);
//                        PhoneNumberUtils.formatNumber(e, PhoneNumberUtils.getFormatTypeForLocale(l));
//                        protasiTemp.number = e.toString();
//                        Log.i("Number formatting", "converted to " + protasiTemp.number);
//                        //}
//                        //else
//                        //{
//                        //	PhoneNumberUtils.formatNumber(protasi.number, country);
//                        //}
//                    }
//                    //else
//                    numbersToResolve.add(protasiTemp.number);
//                    protaseisToResolve.add(protasiTemp);
//
//                } else
//                    finalprotaseis.get(x).network = nn;
//            }

            //Update carrier data
            /*
             * if is internet available
             * get all numbers in final protasi list
             * 	find the ones who we don't have data for
             * 	send to http post service
             * 	receive data
             * 	put data back into database
             * from internal database, update each Protasi in finalprotaseis with carrier info
             * we cool
             *
             */
//        if (isNetworkAvailable() && preferences.getBoolean("resolve_numbers", false))
//        {
//
//            Log.i("Protaseis", "  http-check "+numbersToResolve.size()+" protaseis");
//            if(numbersToResolve.size()>0)
//            {
//                //send numbers to http post
//
//                ArrayList<NameValuePair> data = new ArrayList<NameValuePair>();
//                for (int x=0; x<numbersToResolve.size();x++)
//                {
//                    data.add(new BasicNameValuePair("numbers["+x+"]", numbersToResolve.get(x)));
//                }
//
//                HttpPostAsyncTask pt = new HttpPostAsyncTask(this);
//                pt.execute(data);
//
//                //when the task finishes, then the OnNumbersReceived will fire - go there to put the resolved numbers into the db
//                //and then to refresh the listview.
//            }
//
//        }

            //ftiaxnw enan neo arrayAdapter
            arrayAdapter = new MobileArrayAdapter(this, finalprotaseis);

            lista1.setAdapter(arrayAdapter);
            //To vazw gia to onresume
            //wste to edit text na min exei to focus kai emfanizetai to pliktrologio!
            lista1.requestFocus();

            //Filtro///

            // TextFilter

//            searchView.setIconified(false);
//            searchView.setQuery("lol", false);

//            EditText editTxt = (EditText) searchView.findViewById(filterText);
//            //Gia na arxikopoiite to koumpi se kathe onresume
//            editTxt.setText("");

            //Gia na arxikopoiite to koumpi se kathe onresume

            //Arxika vazw to visibility GONE
//            editTxt.setVisibility(View.GONE);

            //an o xristis exei kanei enable to search kai ena apo ta interfaces einai auta me ti megali lista
//            if (showsearch && (
            if (
                    selectedinterface.equalsIgnoreCase("3") ||
                            selectedinterface.equalsIgnoreCase("4") ||
                            selectedinterface.equalsIgnoreCase("5") ||
                            selectedinterface.equalsIgnoreCase("6")
            ) {
                lista1.setTextFilterEnabled(true);
//                editTxt.setVisibility(View.VISIBLE);
            }
//            //diaforetika to krivw
//            else
//                editTxt.setVisibility(View.GONE);

//            editTxt.addTextChangedListener(new TextWatcher() {
//
//                                               @Override
//                                               public void onTextChanged(CharSequence s, int start, int before, int count) {
//                                                   if (count < before) {
//                                                       // We're deleting char so we need to reset the adapter data
//                                                       arrayAdapter.resetData();
//                                                   }
//
//                                                   arrayAdapter.getFilter().filter(s.toString());
//
//                                               }
//
//                                               @Override
//                                               public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//                                               }
//
//                                               @Override
//                                               public void afterTextChanged(Editable s) {
//
//                                               }
//                                           }
//            );
            ////Filtro telos//

            //apo edw kai katw einai o listener otan patisw ena list item
            lista1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapter, View view, int position, long arg) {
                    view.setPressed(true);
                    String numberToCall = "";
                    Protasi resultToCall = arrayAdapter.getItem(position);


                    //se periptwsi pou ginei click panw se seperator
                    if (resultToCall.score == -3.0) {
                        //min Kaneis tipota

                    } else {

                        int value = 0; //an to epilegmeno einai stis protaseis
                        if (resultToCall.suggested) {
                            value = 1;
                        }
//                    tracker.send(
//                            new HitBuilders.EventBuilder()
//                                    .setCategory("List")
//                                    .setAction("Click")
//                                    .setLabel("WasSuggestion")
//                                    .setValue(value)
//                                    .build());

                        if (resultToCall.score == -2.0) //se periptwsei pou kanei click panw se epafi tou contact list
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(resultToCall.contactID));
                            intent.setData(uri);
                            startActivity(intent);

                        } else {
                            numberToCall = resultToCall.number;
                            Intent intent = new Intent(Intent.ACTION_CALL);
                            intent.setData(Uri.parse("tel:" + numberToCall));
                            startActivity(intent);
                        }
                    }
                }
            });

//            //Otan patithei to koumpi twn epafwn
//            ImageButton buttoncontracts = (ImageButton) findViewById(R.id.contactsButton);
//            buttoncontracts.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
////                tracker.send(
////                        new HitBuilders.EventBuilder()
////                                .setCategory("Buttons")
////                                .setAction("Click")
////                                .setLabel("Contact Button")
////                                .setValue(1)
////                                .build());
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://contacts/people"));
//                    startActivity(intent);
//                }
//            });
//
//            ImageButton buttoncalllog = (ImageButton) findViewById(R.id.callButton);
//            buttoncalllog.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
////                tracker.send(
////                        new HitBuilders.EventBuilder()
////                                .setCategory("Buttons")
////                                .setAction("Click")
////                                .setLabel("CallLog Button")
////                                .setValue(1)
////                                .build());
//
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls"));
//                    startActivity(intent);
//                }
//            });
//
//            start_time = System.currentTimeMillis();
        }
    }
//
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }

    private double calcFreq(ArrayList<calllogrecord> calllog, String uniqueCachedName)
    {
        int occurrences = 0;
        int calllogsize = calllog.size();
        double freqscore = 0.0;
        for (calllogrecord callrecord : calllog)
        {
            if (callrecord.CachedName.equals(uniqueCachedName))
                occurrences++;
        }

        freqscore = (double) occurrences/calllogsize;

        return freqscore;
    }

    private double calcRecenc (ArrayList<calllogrecord> calllog, String uniqueCachedName, long recenWin)
    {
        //metatropy tou recency window se milisecs
        //kai sti synexeia aferw to twra apo megethos tou recency window
        long recenWinDiair = recenWin * 3600L * 1000L;
        long nowtime = System.currentTimeMillis();
        recenWin = nowtime - recenWinDiair;
        double recency = 0.0;


        for (calllogrecord callrecord : calllog)
        {
            //epi tis ousias vriskw tin prwti emfanisei tou arithmou sto subcallog
            if (callrecord.CachedName.equals(uniqueCachedName))
            {
                //kai elegxw an i stigm pou egine i klisi einai mesa sto recency window pou thelw (now - window)
                if (callrecord.date > recenWin)
                {
                    recency = (double) (nowtime - callrecord.date)/recenWinDiair;
                    //gia na dwsw megalitero varos stin pio kontines kleiseis afairw apo ti monada
                    recency = 1.0 - recency;
                    return recency;
                }
                else
                    return 0.0;
            }
        }
        return 0.0;
    }

    private double calcScore (double freq, double recency)
    {
        //Ypologizw to score me vasi ta vari pou exoun dothei
        double score = wf * freq + wr * recency;
        return score;
    }

    private ArrayList<calllogrecord> getCallLog (long days, boolean smsSeek, boolean socialSeek
    )
    {

        //Ena ArrayList gia na valw ta tilefwna tou call log pou anikoun sto freq window
        ArrayList<calllogrecord> subcalllog = new ArrayList<calllogrecord>();

        //pairnw tin trexousa xroniki periodo kai aferw tis meres pou thelw wste na ftiaxw to Freg window
        //long freq_window = System.currentTimeMillis() / 1000L;
        long freq_window = System.currentTimeMillis();
        freq_window = freq_window - days*24L*3600L*1000L;


        ContentResolver cr = getContentResolver();
        //edw bazw poies steiles thelw na sikwsw apo to call log
        //http://developer.android.com/reference/android/provider/CallLog.Calls.html
        String[] selectCols = new String[] {CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.CACHED_NAME};

        //Gia na ferw mono tis kliseis kai oxi ta SMS exw prosthesie to logtype
        //MONO gia Samsung tilefwna
        //http://stackoverflow.com/questions/11294563/sms-are-duplicated-as-callssamsung-galaxy-s-ii
        Cursor cur = null;

        try {
            cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "logtype = 100 AND DATE >" + freq_window, null, "DATE DESC");
            if (cur == null)
                cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "DATE >" + freq_window, null, "DATE DESC");
        } catch (SQLiteException e) {
                cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "DATE >" + freq_window, null, "DATE DESC");
        }



        //String number = cur.getColumnIndex( CallLog.Calls.NUMBER );
        //String number = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
        //int date = cur.getColumnIndex( CallLog.Calls.DATE);
        //int cname = cur.getColumnIndex( CallLog.Calls.CACHED_NAME);

        //pernw to noumero ws double
        double tempnumber = 1.0;

        while ( cur.moveToNext() )
        {
            String phNumber = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
            String cachedname = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));
            long callDate = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));

            boolean epafiboolean = true;
            if (cachedname == null)
            {
                epafiboolean = false;
                cachedname = phNumber;

                if(!getContactName(phNumber).equals(""))
                {
                    cachedname = getContactName(phNumber);
                    epafiboolean = true;
                } else if (phNumber.charAt(0) != '+')
                {
                    //Country Code Bug Temp Fix
                    phNumber = "+" + GetCountryZipCode() + phNumber;
                    if(getContactName(phNumber).equals("")){
                        cachedname = phNumber;
                    } else {
                        cachedname = getContactName(phNumber);
                        epafiboolean = true;
                    }
                }
            }

            //allos ena elegxos gia ta noumera me apokripsi
            if (!phNumber.isEmpty())
            {
                calllogrecord temprecord = new calllogrecord(phNumber,callDate,cachedname,epafiboolean);

                //Se periptwsi pou i klisi einai me apokripsi to pedio phNumber einai arnitikos arithmos
                //Se periptwsi pou exw asterakia stis kliseis p.x. endoetairika WIND *2145
                //boolean isnumeric = true;
                try
                {
                    tempnumber = Double.parseDouble(phNumber);
                }
                catch (NumberFormatException nfe)
                {
                    //isnumeric = false;
                }

                if (tempnumber > 0)
                    subcalllog.add(temprecord);
            }
        }

        cur.close();

        //SMS Log Seeker
        if(smsSeek) {
            Uri uriSms = Uri.parse("content://sms/inbox");
            String[] selectSMSCols = new String[]{"_id", "address", "date"};
            Cursor SMScursor = null;


            try {
                SMScursor = cr.query(uriSms, selectSMSCols, "logtype = 100 AND DATE >" + freq_window, null, "DATE DESC");
                if (SMScursor == null)
                    SMScursor = cr.query(uriSms, selectSMSCols, "DATE >" + freq_window, null, "DATE DESC");
            } catch (SQLiteException e) {
                SMScursor = cr.query(uriSms, selectSMSCols, "DATE >" + freq_window, null, "DATE DESC");
            }

            while (SMScursor.moveToNext()) {
                String SMSphNumber = SMScursor.getString(1);
                long SMScallDate = SMScursor.getLong(2);

                String cachedname = null;
                boolean epafiboolean = true;

//                if((SMSphNumber.equals("12572")) || (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) ){
                if (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) {
                    Log.d("Mobile", "--> " + SMSphNumber);

                    epafiboolean = false;
                    cachedname = SMSphNumber;

                    if (!getContactName(SMSphNumber).equals("")) {
                        cachedname = getContactName(SMSphNumber);
                        epafiboolean = true;
                    } else if (SMSphNumber.charAt(0) != '+') {
                        //Country Code Bug Temp Fix
                        SMSphNumber = "+" + GetCountryZipCode() + SMSphNumber;
                        if (getContactName(SMSphNumber).equals("")) {
                            cachedname = SMSphNumber;
                        } else {
                            cachedname = getContactName(SMSphNumber);
                            epafiboolean = true;
                        }
                    }

                    subcalllog.add(new calllogrecord(SMSphNumber, SMScallDate, cachedname, epafiboolean));

                }
            }

            SMScursor.close();
        }

        db = new NotificationDBHelper(this);

        Log.i("Social", "Notifications Count --> " + db.getNotificationsCount());

        //Social Log Seeker
        if(socialSeek && db.getNotificationsCount() > 0) {
            sdb = db.getWritableDatabase();
            String table = "notifications";
            String[] columns = {"id", "timestamp", "contact"};
            String orderBy = "timestamp DESC";
            String limit = null;

            Cursor SOCIALcursor = sdb.query(table, columns, "timestamp >" + freq_window, null, null, null, orderBy, limit);

            while (SOCIALcursor.moveToNext()) {

                String SOCIALcachedname = SOCIALcursor.getString(2);
                String SOCIALphNumber = getPhoneNumber(SOCIALcachedname);
                long SOCIALDate = SOCIALcursor.getLong(1);
                boolean SOCIALepafiboolean = true;

                if(!SOCIALphNumber.equals("")) {
                    Log.i("Social", "MPHKE1--> " + SOCIALphNumber + " | " + SOCIALDate);
                    subcalllog.add(new calllogrecord(SOCIALphNumber, SOCIALDate, SOCIALcachedname, SOCIALepafiboolean));
                } else{
                    SOCIALphNumber = SOCIALcachedname;
                    SOCIALepafiboolean = false;
                    if (SOCIALphNumber.length() > 9 && SOCIALphNumber.matches("[+]?[0-9]+")) {
                        Log.i("Social", "MPHKE2--> " + SOCIALphNumber + " | " + SOCIALDate);
                        subcalllog.add(new calllogrecord(SOCIALphNumber, SOCIALDate, SOCIALcachedname, SOCIALepafiboolean));
                    }
                }
            }

            SOCIALcursor.close();
        }

        if(smsSeek || socialSeek)
        {
            //sortarw tom subcallrecord ws pros to date
            Collections.sort(subcalllog, new SortDateCallLogRecord());
        }

        return subcalllog;

    }

    private HashSet<String> getUniqueCallRecords (ArrayList<calllogrecord> calllog)
    {
        //dimourgo ena HashSet wste na vrw tis monadikes eggrafes sto to call log pou dinw ws orisma
        HashSet<String> subcallunique = new HashSet<String>();

        for (calllogrecord callrecord: calllog) {
            subcallunique.add(callrecord.CachedName);
        }

        for (String s : subcallunique) {
            Log.i("UNIQUE SUGGESTIONS", "-->" + s);
        }

        return subcallunique;

    }

    private Bitmap loadPhoto (long id)
    {
        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream photoInput = ContactsContract.Contacts.openContactPhotoInputStream(this.getContentResolver(), photoUri);
        if (photoInput != null)
        {
            return BitmapFactory.decodeStream(photoInput);
        }
        return null;
    }

    public String getContactID (String number)
    {
        String contactID = "";
        ContentResolver context = getContentResolver();

        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));

        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup._ID };

        Cursor cur = context.query(lookupUri,mPhoneNumberProjection, null, null, null);
        try
        {
            if (cur.moveToFirst())
            {
                contactID = cur.getString(0);
                return contactID;
            }
        }
        finally
        {
            if (cur != null)
                cur.close();
        }
        return contactID;
    }

    private String getContactName (String number)
    {
        String contactName = "";
        ContentResolver context = getContentResolver();

        /// number is the phone number
        Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(number));

        String[] mPhoneNumberProjection = { ContactsContract.PhoneLookup.DISPLAY_NAME };

        Cursor cur = context.query(lookupUri,mPhoneNumberProjection, null, null, null);
        try
        {
            if (cur.moveToFirst())
            {
                contactName = cur.getString(0);
                return contactName;
            }
        }
        finally
        {
            if (cur != null)
                cur.close();
        }
        return contactName;
    }

    public String getPhoneNumber(String name) {
        String ret = "";
        ContentResolver context = getContentResolver();
        String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME+" like'%" + name +"%'";
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor c = context.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, selection, null, null);

        try
        {
            if (c.moveToFirst())
            {
                // regex gia na vgazei ta whitespaces apo to phone number
                ret = c.getString(0).replaceAll("\\s+","");
                return ret;
            }
        }
        finally
        {
            if (c != null)
                c.close();
        }
        return ret;
    }

    private ArrayList<Protasi> getAllContactsWithoutProtaseis (boolean withphotos, ArrayList<String> protaseisContactsIds)
    {

        ArrayList<Protasi> mycontactlist = new ArrayList<Protasi>();

        //Prosthetw stin ousia to header to Call Log
        //Vazontas mia fake "kataxwrisi" me arithmo tilefwnou -1 kai onoma Call Log
        //Des to MobileArrayAdapter.java
        Protasi mycalllogrecordnull = new Protasi("-1", "Contacts", 0.0, 0.0, -3.0, false, "");
        mycontactlist.add(mycalllogrecordnull);

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null , null, ContactsContract.Contacts.DISPLAY_NAME);
        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {

                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                int hasphonenumber = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                //se periptwsi pou to id einai mesa sto arraylist twn protasewn
                //h' i epafi den exei kanena noumero HAS_PHONE_NUMBER == 0
                //paw stin epomeni
                if (protaseisContactsIds.contains(id) || hasphonenumber == 0)
                    continue;

                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));



                Protasi mycalllogrecord = new Protasi("", name, 0.0, 0.0, -2.0, true, id);

                if (withphotos)
                {
                    //an thelw kai tis photos
                    long lid = Long.parseLong(id);
                    Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, lid);
                    InputStream photoInput = ContactsContract.Contacts.openContactPhotoInputStream(this.getContentResolver(), photoUri);
                    if (photoInput != null)
                    {
                        mycalllogrecord.photo = BitmapFactory.decodeStream(photoInput);
                    }
                    else
                        mycalllogrecord.photo = null;
                }



                mycontactlist.add(mycalllogrecord);
            }
        }
        cur.close();
        return mycontactlist;
    }

//    public void onNumbersReceived(ArrayList<NumberNetwork> numbers) {
//        // TODO Auto-generated method stub
//        if(numbers!=null)
//        {
//            StoreStatsSQLlite sqlHelper = new StoreStatsSQLlite(this);
//
//            /*take each resolved number
//             * put in database
//             * refresh final protaseis list
//             */
//            for (int x=0;x<numbers.size();x++)
//            {
//                NumberNetwork nn = numbers.get(x);
//                sqlHelper.insertNumber(nn.number, nn.mccmnc, nn.networkName, 0);
//            }
//            Log.i("OnNumbersReceived", "Done inserting");
//
//            for (int x=0;x<finalprotaseis.size();x++)
//            {
//                String nn = sqlHelper.getNetworkName(finalprotaseis.get(x).number);
//                if (nn!=null)
//                    finalprotaseis.get(x).network=nn;
//            }
//            arrayAdapter.notifyDataSetChanged();
//        }
//        else
//            Toast.makeText(this, "Unable to retrieve network data, please try later!", Toast.LENGTH_SHORT);
//
//    }

    public String GetCountryZipCode(){
        String CountryID="";
        String CountryZipCode="";

        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for(int i=0;i<rl.length;i++){
            String[] g=rl[i].split(",");
            if(g[1].trim().equals(CountryID.trim())){
                CountryZipCode=g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    private void setupWindowAnimations() {
        Fade fade = new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);

        Slide slide = new Slide();
        slide.setDuration(1000);
        getWindow().setReturnTransition(slide);
    }

    private void changeInterceptedNotification(int notificationCode, long postTime, String Contact){
        switch(notificationCode){
            case NotificationListener.InterceptedNotificationCode.VIBER_CODE:
                //do something, its Viber
                Log.i("Viber", postTime + "| " + Contact);
                //if(Contact.charAt(0) == '+') Contact.replaceAll("\\s+","");
                if(!Contact.equals("Viber"))
                {
                    if(Contact.charAt(0) == '+') { Contact = Contact.replaceAll("\\s+",""); }
                    db.addNotification(postTime, Contact);
                    finish();
                    startActivity(getIntent());
                }
                break;
            case NotificationListener.InterceptedNotificationCode.WHATSAPP_CODE:
                //do something, its WhatsApp
                if(!Contact.equals("WhatsApp"))
                {
                    if(Contact.charAt(0) == '+') { Contact = Contact.replaceAll("\\s+",""); }
                    Log.i("WhatsApp", postTime + "| " + Contact);
                    db.addNotification(postTime, Contact);
                    finish();
                    startActivity(getIntent());
                }
                break;
            case NotificationListener.InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE:
                //do something, its other notification
                break;
//            case NotificationListenerExampleService.InterceptedNotificationCode.FACEBOOK_CODE:
//                //do something, its Facebook
//                break;
//            case NotificationListenerExampleService.InterceptedNotificationCode.INSTAGRAM_CODE:
//                //do something, its Instagram
//                break;
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     */
    private boolean isNotificationServiceEnabled(){
        String pkgName = getPackageName();
        final String flat = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     * */
    public class SuggestionsChangeBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int receivedNotificationCode = intent.getIntExtra("Notification Code",-1);
            long receivedPostTime = intent.getLongExtra("Post Time",-1);
            String receivedContact = intent.getStringExtra("Contact");
            changeInterceptedNotification(receivedNotificationCode, receivedPostTime, receivedContact);
        }
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    private AlertDialog buildNotificationServiceAlertDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Calchas Notification Listener");
        alertDialogBuilder.setMessage("For Calchas to work you need to enable the Notification Listener Service. Enable it now?");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                });
        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // If you choose to not enable the notification listener
                        // the app. will not work as expected
                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                        sharedPreferences.edit().putBoolean("socialseek", false).commit();
                    }
                });
        return(alertDialogBuilder.create());
    }

}

