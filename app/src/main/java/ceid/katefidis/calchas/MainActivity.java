package ceid.katefidis.calchas;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;


class calllogrecord
{
    String number;
    long date;
    String CachedName;
    boolean isContact;
    String type;

    public calllogrecord(String num, long mydate, String CachedName, boolean isContact, String type)
    {
        this.number = num;
        this.date = mydate;
        this.CachedName = CachedName;
        this.isContact = isContact;
        this.type = type;
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
    String type;
    Bitmap photo;
    long date;
    boolean suggested;
    String network;

    public Protasi(String num, String name,double scoref, double scorer, double score, boolean isContact, String type, String contactID)
    {
        this.number = num;
        this.score = score;
        this.name = name;
        this.isContact = isContact;
        this.type = type;
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
    ExpandableListView lista1;
    private int lastExpandedPosition = -1;
//    //	Tracker tracker;
//    Long start_time;
//    Long end_time;
    //HashMap<String, Integer> countryCodes;
    private NotificationDBHelper db;
    private SQLiteDatabase sdb;

    EventDetails event_details;
    BroadcastReceiver activityBroadcastReceiver;


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
//                            .addToBackStack(null)
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
//            /fm.popBackStack();
//            onResume();
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    private SuggestionsChangeBroadcastReceiver suggestionsChangeBroadcastReceiver;
    private AlertDialog enableNotificationListenerAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        long startTime = System.currentTimeMillis();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean DarkMode = settings.getBoolean("DarkMode", false);
        boolean socialSeek = settings.getBoolean("socialseek", false);
        boolean firstRun = settings.getBoolean("firstRun", true);

        if(DarkMode) {
            setTheme(R.style.DarkTheme);
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);


        if(firstRun){
            if(!Permissions.Check_PERMISSIONS(MainActivity.this))
            {
                //if not permisson granted so request permisson with request code
                Permissions.Request_PERMISSIONS(MainActivity.this,1);
            }
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

//        if ( firstRun )
//        {
//
//            startActivityForResult(
//                    new Intent(this, ConsentActivity.class),
//                    1);
//
//        }


        //Activity Tracking
        activityBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("activity_intent")) { //BROADCAST_DETECTED_ACTIVITY
                    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences.Editor editor = settings.edit();
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    Log.d("Activity", "-->" + type + ", " + confidence);
                    if (confidence > 70) { //CONFIDENCE
                        editor.putInt("activityType", type);
                        editor.putInt("activityConfidence", confidence);
                        editor.apply();
                    }
                }
            }
        };
//        LocalBroadcastManager.getInstance(this).registerReceiver(activityBroadcastReceiver, new IntentFilter("activity_intent"));

        startTracking();

        long endTime = System.currentTimeMillis();

        Log.i("Time", "onCreate() took " + (endTime - startTime) + " milliseconds");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(suggestionsChangeBroadcastReceiver);
        stopTracking();
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
                //if lastexpandedgroup is expanded..collapse it!
                if(lista1.isGroupExpanded(lastExpandedPosition)) lista1.collapseGroup(lastExpandedPosition);
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

    private Parcelable state;
    @Override
    public void onPause() {
        try {
            //apothikeusai thn katastasi ths listas otan paei onPause
            state = lista1.onSaveInstanceState();
        } catch (Exception e) {
            Log.d("Error", "App has crashed onPause()");
        }
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityBroadcastReceiver);

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

        long startTime = System.currentTimeMillis();

//        //start new tracker session
//        tracker.send(new HitBuilders.AppViewBuilder()
//                .setNewSession()
//                .build());

        if (Permissions.Check_PERMISSIONS(MainActivity.this)) {
            //init database
            //StoreStatsSQLlite db = new StoreStatsSQLlite(this);
            new CalchasAsyncTask().execute();
        }

        long endTime = System.currentTimeMillis();

        LocalBroadcastManager.getInstance(this).registerReceiver(activityBroadcastReceiver, new IntentFilter("activity_intent"));

        Log.i("Time", "onResume() took " + (endTime - startTime) + " milliseconds");

    }
//
//    private boolean isNetworkAvailable() {
//        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
//        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
//    }


    private class CalchasAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //loading spinner
            ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);

            //set spinner visible / pre calculations
            if(spinner != null && spinner.getVisibility() != View.GONE) spinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {

            //DEVELOPER Epeidi ta evgala exw gia to dump!!!
            subcalllog = new ArrayList<calllogrecord>();
            finalprotaseis = new ArrayList<Protasi>();

            //pernw apo ta user preferences tis antistoixes times
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            String strNumProtaseis = preferences.getString("protaseis", "8");
            final String selectedinterface = preferences.getString("inteface", "3");
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

            //ListView lista1 = (ListView) findViewById(R.id.list);

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
                Protasi prot = new Protasi("NoNumberYet", uniqueCachedName, freqscore, recenscore, calcScore(freqscore, recenscore), false, null, "");
                protaseis.add(prot);

            }

            //Thelw tis top N protaseis apo to ArrayList protaseis me vasi to megalitero score
            //sortarw me to custom comperator
            Collections.sort(protaseis, new CompareProtaseis());

            //Krataw mono tis N protes protaseis
            //protaseis.subList(0, numProtaseis-1);


            //DEVEOPER OPTION
            //final ArrayList<protasi> finalprotaseis = new ArrayList<protasi>();

            String protaseisDB="";
            String protaseis_last_channelDB="";

            int i = 0;
            for (Protasi protasitemp : protaseis) {

                for (calllogrecord s1 : subcalllog) {
                    if (protasitemp.name.equals(s1.CachedName)) {
                        protasitemp.number = s1.number;
                        protasitemp.date = s1.date;
                        protasitemp.type = s1.type;
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

                if(protasitemp.isContact)
                protaseisDB += protasitemp.contactID + ", ";
                else  {
                    //encrypt plain number text to md5
                    protaseisDB += md5encrypt(protasitemp.number) + ", ";
                }

                if (protasitemp.type.equals("phone")) {
                    protaseis_last_channelDB += 1 + ", ";
                } else if (protasitemp.type.equals("viber")) {
                    protaseis_last_channelDB += 3 + ", ";
                } else if (protasitemp.type.equals("whatsapp")) {
                    protaseis_last_channelDB += 4 + ", ";
                } else protaseis_last_channelDB += 2 + ", ";

                finalprotaseis.add(protasitemp);

                i++;
                //Thelw tis N kalyteres protaseis
                //To 9 isodynamei me to na deixei oles tis protaseis ara den thelw to break
                if (i > numProtaseis - 1 && numProtaseis != 9)
                    break;
            }

            if(protaseisDB.length() > 1) { //uparxoun protaseis
                protaseisDB = protaseisDB.substring(0, protaseisDB.length() - 2);
                protaseis_last_channelDB = protaseis_last_channelDB.substring(0, protaseis_last_channelDB.length() - 2);

                String deviceID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                event_details = new EventDetails(deviceID, protaseisDB, protaseis_last_channelDB);

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
                                Protasi mycalllogrecord = new Protasi(singlecallrecord.number, singlecallrecord.CachedName, 0.0, 0.0, 0.0, singlecallrecord.isContact, singlecallrecord.type, "");
                                mycalllogrecord.date = singlecallrecord.date;

                                //se periptwsi pou thenw na emfanizw kai tis photos sto unique call log
                                if (showphoto && mycalllogrecord.isContact) {
                                    try {
                                        mycalllogrecord.contactID = getContactID(mycalllogrecord.number);
                                        try {
                                            mycalllogrecord.photo = loadPhoto(Long.parseLong(mycalllogrecord.contactID));
                                        } catch (NumberFormatException nfe)
                                        {
                                            Log.d("Photo", "contact id is not a number");
                                        }
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

                //an einai empty to callog, mhn to valeis
                if (protaseis.size() > numProtaseis){
                    //meta to sort vazw ton seperator prwto sti lista
                    Protasi mycalllogrecordnull = new Protasi("-1", "Recent Calls", 0.0, 0.0, -3.0, false, null, "");
                    mycalllog.add(0, mycalllogrecordnull);
                }

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


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            final String selectedinterface = preferences.getString("inteface", "3");
            //loading spinner
            ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);

            //ftiaxnw enan neo arrayAdapter


            arrayAdapter = new MobileArrayAdapter(MainActivity.this, finalprotaseis, event_details);
            lista1 = (ExpandableListView) findViewById(R.id.list);


            try {
                lista1.setAdapter(arrayAdapter);

                //spinner hide
                spinner.setVisibility(View.GONE);
                //To vazw gia to onresume
                //wste to edit text na min exei to focus kai emfanizetai to pliktrologio!
                lista1.requestFocus();

                //an to saved state sto pause den einai miden, epanefere thn katastash ths listas.
                if (state != null){
                    lista1.onRestoreInstanceState(state);
                    arrayAdapter.notifyDataSetChanged();
                }



                lista1.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                    @Override
                    public void onGroupExpand(int groupPosition) {
                        if(lastExpandedPosition != -1 && groupPosition != lastExpandedPosition){
                            lista1.collapseGroup(lastExpandedPosition);
                        }
                        lastExpandedPosition = groupPosition;
                    }
                });

                lista1.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                    @Override
                    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                        Protasi prot = (Protasi) arrayAdapter.getGroup(i);
                        Log.d("ProtaseisDB", "Clicked -->" + prot.contactID);
                        if(prot.score == -3.0){
                            return true;
                        } else if(prot.score == -2.0){
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(prot.contactID));
                            intent.setData(uri);
                            startActivity(intent);
                            //arrayAdapter.notifyDataSetChanged();
                            //lista1.setSelectedGroup(0);
                            return true;
                        }
                        return false;
                    }
                });


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

            } catch (Exception e) {
                Log.d("Crash", "Error on setAdapter()!");
            }

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

    private ArrayList<calllogrecord> getCallLog (long days, boolean smsSeek, boolean socialSeek) {
        long startTime = System.currentTimeMillis();
        //Ena ArrayList gia na valw ta tilefwna tou call log pou anikoun sto freq window
        ArrayList<calllogrecord> subcalllog = new ArrayList<calllogrecord>();

        //pairnw tin trexousa xroniki periodo kai aferw tis meres pou thelw wste na ftiaxw to Freg window
        //long freq_window = System.currentTimeMillis() / 1000L;
        long freq_window = System.currentTimeMillis();
        freq_window = freq_window - days * 24L * 3600L * 1000L;


        ContentResolver cr = getContentResolver();
        //edw bazw poies steiles thelw na sikwsw apo to call log
        //http://developer.android.com/reference/android/provider/CallLog.Calls.html
        String[] selectCols = new String[]{CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.CACHED_NAME};
        //Gia na ferw mono tis kliseis kai oxi ta SMS exw prosthesie to logtype
        //MONO gia Samsung tilefwna
        //http://stackoverflow.com/questions/11294563/sms-are-duplicated-as-callssamsung-galaxy-s-ii
        Cursor cur = null;


        long startTimeBug = System.currentTimeMillis();
        //CACHED_NAME Android Bug/Duplication Protasi Bug Fix
        String[] FirstRowBug_selectCols = new String[]{CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME};

        try {
            cur = cr.query(CallLog.Calls.CONTENT_URI, FirstRowBug_selectCols, "logtype = 100 AND DATE >" + freq_window, null, "DATE DESC LIMIT 5");
            if (cur == null)
                cur = cr.query(CallLog.Calls.CONTENT_URI, FirstRowBug_selectCols, "DATE >" + freq_window, null, "DATE DESC LIMIT 10");
        } catch (SQLiteException e) {
            cur = cr.query(CallLog.Calls.CONTENT_URI, FirstRowBug_selectCols, "DATE >" + freq_window, null, "DATE DESC LIMIT 10");
        }


        //pernw to noumero ws double
        double tempnumber = 1.0;

        //cur.moveToFirst();
        while (cur.moveToNext())
        {
            Log.i("Bug", cur.getString(cur.getColumnIndex(CallLog.Calls._ID)) + "|" + cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME)) + "|" + cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
            Integer FirstRowBug_ID = Integer.parseInt(cur.getString(cur.getColumnIndex(CallLog.Calls._ID)));
            String FirstRowBug_temp_cached_name = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));
            if (FirstRowBug_temp_cached_name == null) {
                String FirstRowBug_CACHED_NAME = getContactName(cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER)));
                if (FirstRowBug_CACHED_NAME != null)
                    updateCachedName(cr, FirstRowBug_ID, FirstRowBug_CACHED_NAME);
            }
        }
        cur.close();

        long endTimeBug = System.currentTimeMillis();

        Log.i("Time", "1. Bug Correction took " + (endTimeBug - startTimeBug) + " milliseconds");

        long startTimeQuery = System.currentTimeMillis();

        try {
            cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "logtype = 100 AND DATE >" + freq_window, null, "DATE DESC");
            if (cur == null)
                cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "DATE >" + freq_window, null, "DATE DESC");
        } catch (SQLiteException e) {
            cur = cr.query(CallLog.Calls.CONTENT_URI, selectCols, "DATE >" + freq_window, null, "DATE DESC");
        }


        while ( cur.moveToNext() )
        {
            String phNumber = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
            String cachedname = cur.getString(cur.getColumnIndex(CallLog.Calls.CACHED_NAME));
            long callDate = cur.getLong(cur.getColumnIndex(CallLog.Calls.DATE));

            boolean epafiboolean = true;

            if (cachedname == null) {
                epafiboolean = false;
                cachedname = phNumber;
            }

            //allos ena elegxos gia ta noumera me apokripsi
            if (!phNumber.isEmpty())
            {
                calllogrecord temprecord = new calllogrecord(phNumber,callDate,cachedname,epafiboolean, "phone");

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

        long endTimeQuery = System.currentTimeMillis();

        Log.i("Time", "3. Call Log Query took " + (endTimeQuery - startTimeQuery) + " milliseconds");

        //SMS Log Seeker
        if(smsSeek) {

            //SMS Inbox Query
            Uri uriSmsInbox = Uri.parse("content://sms/inbox");
            String[] selectSMSCols = new String[]{"_id", "address", "date"};
            Cursor SMSinboxcursor = cr.query(uriSmsInbox, selectSMSCols, "DATE >" + freq_window, null, "DATE DESC");

            while (SMSinboxcursor.moveToNext()) {
                String SMSphNumber = SMSinboxcursor.getString(1);
                long SMScallDate = SMSinboxcursor.getLong(2);
                String SMScachedname;
                boolean SMSepafiboolean;

//                if((SMSphNumber.equals("12572")) || (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) ){
                if (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) {

                    SMSepafiboolean = false;
                    SMScachedname = SMSphNumber;

                    String tempCachedName = getContactName(SMSphNumber);
                    if (!tempCachedName.equals(""))
                    {
                        SMScachedname = tempCachedName;
                        SMSepafiboolean = true;
                    } else if (SMSphNumber.charAt(0) != '+')
                    {
                        //Country Code Bug Temp Fix
                        String tempPhNumber = "+" + GetCountryZipCode() + SMSphNumber;
                        String tempCachedNameCode = getContactName(tempPhNumber);
                        if(!tempCachedNameCode.equals("")){
                            SMScachedname = tempCachedNameCode;
                            SMSepafiboolean = true;
                        }
                    }

                    subcalllog.add(new calllogrecord(SMSphNumber, SMScallDate, SMScachedname, SMSepafiboolean, "sms"));

                }
            }
            SMSinboxcursor.close();

            //SMS Sent Query
            Uri uriSmsSent = Uri.parse("content://sms/sent");
            Cursor SMSsentcursor = cr.query(uriSmsSent, selectSMSCols, "DATE >" + freq_window, null, "DATE DESC");

            while (SMSsentcursor.moveToNext()) {
                String SMSphNumber = SMSsentcursor.getString(1);
                long SMScallDate = SMSsentcursor.getLong(2);
                String SMScachedname;
                boolean SMSepafiboolean;

//                if((SMSphNumber.equals("12572")) || (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) ){
                if (SMSphNumber.length() > 9 && SMSphNumber.matches("[+]?[0-9]+")) {

                    SMSepafiboolean = false;
                    SMScachedname = SMSphNumber;

                    String tempCachedName = getContactName(SMSphNumber);
                    if (!tempCachedName.equals(""))
                    {
                        SMScachedname = tempCachedName;
                        SMSepafiboolean = true;
                    } else if (SMSphNumber.charAt(0) != '+')
                    {
                        //Country Code Bug Temp Fix
                        String tempPhNumber = "+" + GetCountryZipCode() + SMSphNumber;
                        String tempCachedNameCode = getContactName(tempPhNumber);
                        if(!tempCachedNameCode.equals("")){
                            SMScachedname = tempCachedNameCode;
                            SMSepafiboolean = true;
                        }
                    }

                    subcalllog.add(new calllogrecord(SMSphNumber, SMScallDate, SMScachedname, SMSepafiboolean, "sms"));

                }
            }
            SMSsentcursor.close();

        }

        db = new NotificationDBHelper(this);

        Log.i("Social", "Notifications Count --> " + db.getNotificationsCount());

        //Social Log Seeker
        if(socialSeek && db.getNotificationsCount() > 0) {
            sdb = db.getWritableDatabase();
            String table = "notifications";
            String[] columns = {"id", "timestamp", "contact", "type"};
            String orderBy = "timestamp DESC";
            String limit = null;

            Cursor SOCIALcursor = sdb.query(table, columns, "timestamp >" + freq_window, null, null, null, orderBy, limit);

            while (SOCIALcursor.moveToNext()) {

                String SOCIALcachedname = SOCIALcursor.getString(2);
                String SOCIALphNumber = getPhoneNumber(SOCIALcachedname);
                long SOCIALDate = SOCIALcursor.getLong(1);
                String SOCIALtype = SOCIALcursor.getString(3);
                boolean SOCIALepafiboolean = true;

                if(!SOCIALphNumber.equals("")) {
                    //Log.i("Social", "MPHKE1--> " + SOCIALphNumber + " | " + SOCIALDate);
                    subcalllog.add(new calllogrecord(SOCIALphNumber, SOCIALDate, SOCIALcachedname, SOCIALepafiboolean, SOCIALtype));
                } else{
                    if(SOCIALtype.equals("whatsapp")) //Viber Contact/No Contact Bug Fix
                    {
                        SOCIALphNumber = SOCIALcachedname;
                        SOCIALepafiboolean = false;
                        if (SOCIALphNumber.length() > 9 && SOCIALphNumber.matches("[+]?[0-9]+")) {
                            //Log.i("Social", "MPHKE2--> " + SOCIALphNumber + " | " + SOCIALDate);
                            subcalllog.add(new calllogrecord(SOCIALphNumber, SOCIALDate, SOCIALcachedname, SOCIALepafiboolean, SOCIALtype));
                        }
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

        long endTime = System.currentTimeMillis();

        Log.i("Time", "getCallLog() took " + (endTime - startTime) + " milliseconds");

        return subcalllog;

    }

    private HashSet<String> getUniqueCallRecords (ArrayList<calllogrecord> calllog)
    {
        //dimourgo ena HashSet wste na vrw tis monadikes eggrafes sto to call log pou dinw ws orisma
        HashSet<String> subcallunique = new HashSet<String>();

        for (calllogrecord callrecord: calllog) {
            subcallunique.add(callrecord.CachedName);
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

    public void updateCachedName(ContentResolver cr, int id, @NonNull String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CallLog.Calls.CACHED_NAME, name);
        cr.update(CallLog.Calls.CONTENT_URI, contentValues, CallLog.Calls._ID + "=" + id, null);
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
        Protasi mycalllogrecordnull = new Protasi("-1", "Contacts", 0.0, 0.0, -3.0, false, null, "");
        mycontactlist.add(mycalllogrecordnull);

        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER }, ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'", null, ContactsContract.Contacts.DISPLAY_NAME);

        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {

                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                //se periptwsi pou to id einai mesa sto arraylist twn protasewn
                //paw stin epomeni
                if (protaseisContactsIds.contains(id))
                    continue;

                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Protasi mycalllogrecord = new Protasi("", name, 0.0, 0.0, -2.0, true, null,  id);

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

    private String md5encrypt(String numberToEncrypt)
    {
        //md5 number encryption
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(numberToEncrypt.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String hashNumber = number.toString(16);
            return hashNumber;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
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
                    db.addNotification(postTime, Contact, "viber");
                    onResume(); //refresh
                }
                break;
            case NotificationListener.InterceptedNotificationCode.WHATSAPP_CODE:
                //do something, its WhatsApp
                if(!Contact.equals("WhatsApp"))
                {
                    if(Contact.charAt(0) == '+') { Contact = Contact.replaceAll("\\s+",""); }
                    Log.i("WhatsApp", postTime + "| " + Contact);
                    db.addNotification(postTime, Contact, "whatsapp");
                    onResume(); //refresh
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
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived
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

    private void startTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent);
    }

    private void stopTracking() {
        Intent intent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        stopService(intent);
    }

}

