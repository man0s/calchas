package ceid.katefidis.calchas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.location.Location;

import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.BATTERY_SERVICE;
import static android.content.Context.SENSOR_SERVICE;


public class MobileArrayAdapter extends BaseExpandableListAdapter implements Filterable {
    private final Context context;
    private Filter contactFilter;
    private ArrayList<Protasi> protaseis;
    private ArrayList<Protasi> originprotaseis;
    private int colorIndex = 0;

    private EventDetails event_details;
    final boolean[] flag = new boolean[1];


    public MobileArrayAdapter(Context context, ArrayList<Protasi> protaseis, EventDetails event_details) {
        //super(context, R.layout.list_protaseis, protaseis);
        this.context = context;
        this.protaseis = protaseis;
        this.originprotaseis = protaseis;
        this.event_details = event_details;
    }

    @Override
    public View getGroupView(int position, boolean isExpanded,
                             View rowView, ViewGroup parent) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean DarkMode = settings.getBoolean("DarkMode", false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rowView = inflater.inflate(R.layout.list_protaseis, parent, false);
//
//        if (rowView == null) {
//            rowView = inflater.inflate(R.layout.list_protaseis, null);
//        }

        colorIndex = position;
        if (colorIndex > 11) colorIndex = colorIndex % 11;

        String[] darkColors = context.getResources().getStringArray(R.array.darkColors);
        String[] lightColors = context.getResources().getStringArray(R.array.lightColors);


        //Pairnw to antikeimeno pou fainetai sto position
//        Protasi prot = protaseis.get(position);
        final Protasi prot = (Protasi) getGroup(position);

        //se periptwsi pou antikeimeno tou list view einai kapoios seperator
        //fernw ws row to seperator.xml
        if (prot.score == -3.0) {
            rowView = inflater.inflate(R.layout.seperator, parent, false);
            rowView.setEnabled(false);
            rowView.setClickable(false);
            TextView SeperatorText = (TextView) rowView.findViewById(R.id.seperator);
            SeperatorText.setText(prot.name);
//            Typeface face = Typeface.createFromAsset(context.getAssets(),
//                    "fonts/Lobster.ttf");
//            SeperatorText.setTypeface(face);

        }
        //se periptwsi pou einai epafi fernw mono badge kai onoma
        else if (prot.score == -2.0) {
            rowView = inflater.inflate(R.layout.contact, parent, false);
            TextView ContactName = (TextView) rowView.findViewById(R.id.contact_name_to_list);
            if (prot.name.length() > 20) {
                String name = prot.name.substring(0, Math.min(prot.name.length(), 20)) + ".";
                ContactName.setText(name);
            } else ContactName.setText(prot.name);

            //if(prot.type != null)  ContactName.setText(prot.name + "|" + prot.type);

            //Gia to badge
            //QuickContactBadge eikonacode = (QuickContactBadge) rowView.findViewById(R.id.contact_photo);
            RoundedQuickContactBadge eikonacode = (RoundedQuickContactBadge) rowView.findViewById(R.id.contact_photo);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(prot.contactID));
            eikonacode.assignContactUri(uri);
            if (prot.photo != null) {
                eikonacode.setImageBitmap(prot.photo);
            } else {
                if (DarkMode) {
                    eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
                    int randomDarkColor = Color.parseColor(darkColors[colorIndex]);
                    eikonacode.setColorFilter(randomDarkColor);
                } else {
                    eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
                    int randomLightColor = Color.parseColor(lightColors[colorIndex]);
                    eikonacode.setColorFilter(randomLightColor);
                }
            }

        } else {
            TextView textView = rowView.findViewById(R.id.contact_name);
            TextView textView1 = rowView.findViewById(R.id.contact_number);
            TextView datecontacted = rowView.findViewById(R.id.datecontacted);
            //TextView network = rowView.findViewById(R.id.network);

            //Gia na efmanizetai i wra kai imerominia tis teleutaias epikoinwnias me tin protasi
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
            String dateString = formatter.format(new Date(prot.date));
            datecontacted.setText(dateString);
            //network.setText(prot.network);
            if (prot.isContact && !prot.name.equals("")) { //to != null se periptwsh pou uparxei fault isContact
                if (prot.name.length() > 20) {
                    String name = prot.name.substring(0, Math.min(prot.name.length(), 20)) + ".";
                    textView.setText(name);
                } else textView.setText(prot.name);
                //if(prot.type != null)   textView.setText(prot.name + "|" + prot.type);
                textView1.setText(prot.number);
            } else {
                //Google Business Bug Fix
                if(!prot.name.matches("^(?:[+]?[0-9]+|)$")) //name not null or number
                {
                    if (prot.name.length() > 20) {
                        String name = prot.name.substring(0, Math.min(prot.name.length(), 20)) + ".";
                        textView.setText(name);
                    } else textView.setText(prot.name);
                    textView1.setText(prot.number);
                } else {
                    textView.setText(prot.number);
                    //if(prot.type != null)   textView.setText(prot.name + "|" + prot.type);
                    textView1.setText("");
                }
            }

            //Gia to badge
            //QuickContactBadge eikonacode = (QuickContactBadge) rowView.findViewById(R.id.contact_photo);
            RoundedQuickContactBadge eikonacode = (RoundedQuickContactBadge) rowView.findViewById(R.id.contact_photo);
            eikonacode.assignContactFromPhone(prot.number, true);

            if (prot.photo != null) {
                eikonacode.setImageBitmap(prot.photo);
            } else {
                if (DarkMode) {
                    eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
                    int randomDarkColor = Color.parseColor(darkColors[colorIndex]);
                    eikonacode.setColorFilter(randomDarkColor);
                } else {
                    eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
                    int randomLightColor = Color.parseColor(lightColors[colorIndex]);
                    eikonacode.setColorFilter(randomLightColor);
                }
            }

            //Gia to icon
            ImageView typeIcon = rowView.findViewById(R.id.type);
            if (prot.type != null) {
                if (prot.type.equals("phone")) {
                    typeIcon.setImageResource(R.drawable.ic_call_24dp);
                } else if (prot.type.equals("viber")) {
                    typeIcon.setImageResource(R.drawable.ic_viber_24dp);
                } else if (prot.type.equals("whatsapp")) {
                    typeIcon.setImageResource(R.drawable.ic_whatsapp_24dp);
                } else typeIcon.setImageResource(R.drawable.ic_sms_24dp);
            }

        }

        final Protasi protasiToCall = prot;

        ImageView callIcon = (ImageView) rowView.findViewById(R.id.call_action);
        if (callIcon != null) {
            callIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL);
                    phoneIntent.setData(Uri.parse("tel:" + protasiToCall.number));
                    context.startActivity(phoneIntent);
                    notifyDataSetChanged();

                    //insert details to db
                    if (!protasiToCall.isContact) {
                        event_details.chosen = md5encrypt(protasiToCall.number);
                    } else event_details.chosen = protasiToCall.contactID;
                    event_details.sf = protasiToCall.scoref;
                    event_details.sr = protasiToCall.scorer;
                    try {
                        insertToDB();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return rowView;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        //parent.setPressed(true);
        final Protasi resultToCall = (Protasi) getChild(groupPosition, childPosition);

        if (convertView == null) {

            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.expanded_protasi, null);
        }

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
                context.startActivity(intent);

            } else {
//                            case 0: //phone
//                                Intent phoneIntent = new Intent(Intent.ACTION_CALL);
//                                phoneIntent.setData(Uri.parse("tel:" + numberToCall));
//                                startActivity(phoneIntent);
//                                break;

                LinearLayout layoutMsg = convertView.findViewById(R.id.msg_expanded);

                layoutMsg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
                        smsIntent.setData(Uri.parse("sms:" + resultToCall.number));
                        context.startActivity(smsIntent);
                        notifyDataSetChanged();

                        //insert details to db
                        if (!resultToCall.isContact) {
                            event_details.chosen = md5encrypt(resultToCall.number);
                        } else event_details.chosen = resultToCall.contactID;
                        event_details.sf = resultToCall.scoref;
                        event_details.sr = resultToCall.scorer;
                        try {
                            insertToDB();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                LinearLayout layoutViber = convertView.findViewById(R.id.viber_expanded);

                layoutViber.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appInstalledOrNot("com.viber.voip")) //if app is installed in the device
                        {
                            Intent viberIntent = new Intent(Intent.ACTION_VIEW);
                            viberIntent.setPackage("com.viber.voip");
                            //start viber even if it's not in the background
                            String apiViber;
                            if (resultToCall.number.charAt(0) != '+') //no country prefix social number encoding fix
                            {
                                String fixedPrefixNumber = GetCountryZipCode() + resultToCall.number;
                                apiViber = "viber://contact?number=" + fixedPrefixNumber;
                            } else
                                apiViber = "viber://contact?number=" + resultToCall.number.replace("+", "");
                            Log.i("API", apiViber);
                            viberIntent.setData(Uri.parse(apiViber));
                            context.startActivity(viberIntent);

                            //insert details to db
                            if (!resultToCall.isContact) {
                                event_details.chosen = md5encrypt(resultToCall.number);
                            } else event_details.chosen = resultToCall.contactID;
                            event_details.sf = resultToCall.scoref;
                            event_details.sr = resultToCall.scorer;
                            try {
                                insertToDB();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(context, "Viber is not installed!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                LinearLayout layoutWhatsApp = convertView.findViewById(R.id.whatsapp_expanded);

                layoutWhatsApp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (appInstalledOrNot("com.whatsapp")) //if app is installed in the device
                        {
                            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                            String apiWhatsApp;
                            if (resultToCall.number.charAt(0) != '+') //no country prefix social number encoding fix
                            {
                                String fixedPrefixNumber = GetCountryZipCode() + resultToCall.number;
                                apiWhatsApp = "https://api.whatsapp.com/send?phone=" + fixedPrefixNumber;
                            } else
                                apiWhatsApp = "https://api.whatsapp.com/send?phone=" + resultToCall.number.replace("+", "");
                            Log.i("API", apiWhatsApp);
                            whatsappIntent.setData(Uri.parse(apiWhatsApp));
                            context.startActivity(whatsappIntent);

                            //insert details to db
                            if (!resultToCall.isContact) {
                                event_details.chosen = md5encrypt(resultToCall.number);
                            } else event_details.chosen = resultToCall.contactID;
                            event_details.sf = resultToCall.scoref;
                            event_details.sr = resultToCall.scorer;
                            try {
                                insertToDB();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(context, "WhatsApp is not installed!", Toast.LENGTH_LONG).show();
                        }

                    }
                });

            }

        }


        Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animation.setDuration(300);
        convertView.startAnimation(animation);


        return convertView;
    }


    @Override
    public int getGroupCount() {
        return this.protaseis.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.protaseis.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.protaseis.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


    public void resetData() {
        protaseis = originprotaseis;
    }

    @Override
    public Filter getFilter() {
        if (contactFilter == null)
            contactFilter = new ProtaseisFilter();

        return contactFilter;
    }

    public Protasi getItem(int position) {
        return protaseis.get(position);
    }

    public int getCount() {
        return protaseis.size();
    }

    @SuppressLint("NewApi")
    private class ProtaseisFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0) {
                // No filter implemented we return all the list
                results.values = protaseis;
                results.count = protaseis.size();
            } else {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean showphoto = preferences.getBoolean("showphoto", true);

                // We perform filtering operation
                ArrayList<Protasi> nProtaseis = new ArrayList<Protasi>();

                ArrayList<Protasi> mycontactlist = getAllContacts(showphoto);

                for (Protasi p : mycontactlist) {
                    //edw einai o tropos me ton opoio ginetai to search
                    String tocheck1 = Normalizer.normalize(p.name, Normalizer.Form.NFD);
                    String tocheck2 = Normalizer.normalize(constraint.toString(), Normalizer.Form.NFD);
                    tocheck1 = tocheck1.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    tocheck2 = tocheck2.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    //tocheck2 = tocheck2.replaceAll("[^\\p{ASCII}]", "");

                    //if (p.name.toUpperCase(Locale.getDefault()).startsWith(constraint.toString().toUpperCase(Locale.getDefault())))
                    //h protash den einai to seperator Recent Calls me arithmo -1
                    if (!p.number.equals("-1")) {
                        if (tocheck1.toUpperCase(Locale.getDefault()).contains(tocheck2.toUpperCase(Locale.getDefault())))
                            nProtaseis.add(p);
                    }
                }

                results.values = nProtaseis;
                results.count = nProtaseis.size();

            }
            return results;
        }

        private ArrayList<Protasi> getAllContacts(boolean withphotos) {

            ArrayList<Protasi> mycontactlist = new ArrayList<Protasi>();

            ContentResolver cr = context.getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER}, ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'", null, ContactsContract.Contacts.DISPLAY_NAME);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {

                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Protasi mycalllogrecord = new Protasi("", name, 0.0, 0.0, -2.0, true, null, id);

                    if (withphotos) {
                        //an thelw kai tis photos
                        long lid = Long.parseLong(id);
                        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, lid);
                        InputStream photoInput = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), photoUri);
                        if (photoInput != null) {
                            mycalllogrecord.photo = BitmapFactory.decodeStream(photoInput);
                        } else
                            mycalllogrecord.photo = null;
                    }


                    mycontactlist.add(mycalllogrecord);
                }
            }
            cur.close();
            return mycontactlist;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            //Now we have to inform the adapter about the new list filtered
            //an to gramma pou tha patithei antistoixei se miden apotelesmata
            //na vgainei kano
            //if (results.count == 0)
            //	notifyDataSetInvalidated();
            //else
            //{

            protaseis = (ArrayList<Protasi>) results.values;
            notifyDataSetChanged();
            //}
        }
    }

    private String GetCountryZipCode() {
        String CountryID = "";
        String CountryZipCode = "";

        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        //getNetworkCountryIso
        CountryID = manager.getSimCountryIso().toUpperCase();
        String[] rl = context.getResources().getStringArray(R.array.CountryCodes);
        for (int i = 0; i < rl.length; i++) {
            String[] g = rl[i].split(",");
            if (g[1].trim().equals(CountryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    private String md5encrypt(String numberToEncrypt) {
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

    private static Integer getConnectivityType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected())
            return 1; //not connected
        if (info.getType() == ConnectivityManager.TYPE_WIFI)
            return 2;
        if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int networkType = info.getSubtype();
            switch (networkType) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN: //api<8 : replace by 11
                    return 3;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B: //api<9 : replace by 14
                case TelephonyManager.NETWORK_TYPE_EHRPD:  //api<11 : replace by 12
                case TelephonyManager.NETWORK_TYPE_HSPAP:  //api<13 : replace by 15
                case TelephonyManager.NETWORK_TYPE_TD_SCDMA:  //api<25 : replace by 17
                    return 3;
                case TelephonyManager.NETWORK_TYPE_LTE:    //api<11 : replace by 13
                case TelephonyManager.NETWORK_TYPE_IWLAN:  //api<25 : replace by 18
                case 19:  //LTE_CA
                    return 3;
                default:
                    return 1;
            }
        }
        return 1;
    }

    private void insertToDB() throws IOException{

        final SharedPreferences activity = PreferenceManager.getDefaultSharedPreferences(context);

        //get battery lvl
        BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
        event_details.battery_level = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

        double[] gps = getGPS(); //get location info from GPS
        event_details.location_coords = gps[0] + ", " + gps[1]; //lat, lng
        event_details.location_accuracy = Double.toString(gps[2]);//location accuracy

        //get connectivity type
        event_details.connectivity = getConnectivityType(context);

        //screen state
        event_details.screen_state = isScreenOn(context);  //screen_state, true for ON, false for OFF

        event_details.ringer_mode = getRingMode(); //0 for error?, 1 for silent mode, 2 for vibrate mode, 3 for normal mode


        Log.i("event_details", event_details.uid + " | " + event_details.chosen + " | " + event_details.sf + " | " + event_details.sr);
        Log.i("event_details_bat", event_details.battery_level + "%");
        Log.i("event_details_conn", String.valueOf(event_details.connectivity));
//        Log.i("event_details_light", String.valueOf(event_details.ambient_light));
        Log.i("event_details_screen", String.valueOf(event_details.screen_state));
        Log.i("event_details_ringer", String.valueOf(event_details.ringer_mode));

        SensorManager mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        Sensor mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        AsyncLightSensor task = new AsyncLightSensor();
        task.doInBackground(mSensorManager);
        mSensorManager.registerListener(task, mLight, SensorManager.SENSOR_DELAY_NORMAL);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 5 seconds
                event_details.activity_type = activity.getInt("activityType", -1);
                event_details.activity_confidence = activity.getInt("activityConfidence", 0);

                Log.i("LOCATION", "--> " + event_details.location_coords +" AMBIENT LIGHT--> " + event_details.ambient_light);
                Log.i("ACTIVITY", "--> " + event_details.activity_type + ", " + event_details.activity_confidence);
                Toast.makeText(context, "(" + event_details.activity_type + ", " + event_details.activity_confidence + ")", Toast.LENGTH_SHORT).show();
                String[] data = { event_details.uid,  //TODO event_details.uid
                        Integer.toString(event_details.did), //TODO event_details.did
                        Integer.toString(event_details.eid), //TODO event_details.eid
                        event_details.protaseis,
                        event_details.chosen,
                        Double.toString(event_details.sf),
                        Double.toString(event_details.sr),
                        event_details.protaseis_last_channel,
                        event_details.location_coords,
                        event_details.location_accuracy,
                        Integer.toString(event_details.screen_state),
                        Integer.toString(event_details.ringer_mode),
                        Integer.toString(event_details.battery_level),
                        Float.toString(event_details.ambient_light),
                        Integer.toString(event_details.connectivity),
                        Integer.toString(event_details.activity_type),
                        Integer.toString(event_details.activity_confidence)
                };
                new AsyncHttpPost().execute(data);
            }
        }, 1000);



    }

    public class AsyncLightSensor extends AsyncTask<SensorManager, Void, Void> implements SensorEventListener {
        private SensorManager sensorManager;

        @Override
        public void onSensorChanged(SensorEvent event) {
            if (!flag[0]) { //run only one time
                event_details.ambient_light = event.values[0];
                flag[0] = true;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { }

        @Override
        protected Void doInBackground(SensorManager... params) {
            sensorManager = params[0];
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            sensorManager.unregisterListener(this);
        }
    }

    private class AsyncHttpPost extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("POST", "Posting session's data to server..");
        }

        @Override
        protected String doInBackground(String... arg) {
            try {

                // POST Request
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("uid", arg[0]);
                postDataParams.put("did", arg[1]);
                postDataParams.put("eid", arg[2]);
                postDataParams.put("protaseis", arg[3]);
                postDataParams.put("chosen", arg[4]);
                postDataParams.put("sf", arg[5]);
                postDataParams.put("sr", arg[6]);
                postDataParams.put("protaseis_last_channel", arg[7]);
                postDataParams.put("String location_coords", arg[8]);
                postDataParams.put("location_accuracy", arg[9]);
                postDataParams.put("screen_state", arg[10]);
                postDataParams.put("ringer_mode", arg[11]);
                postDataParams.put("battery_level", arg[12]);
                postDataParams.put("ambient_light", arg[13]);
                postDataParams.put("connectivity", arg[14]);
                postDataParams.put("activity_type", arg[15]);
                postDataParams.put("activity_confidence", arg[16]);

                return sendPost("https://okeanos.katefidis.ga/calchas/post.php", postDataParams);
            } catch (Exception e) {
                return "Exception: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            Log.i("POST", "Post AsyncTask executed. (" + result + ")");
        }
    }

    private static String sendPost(String r_url, JSONObject postDataParams) throws Exception {
        URL url = new URL(r_url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(20000);
        conn.setConnectTimeout(20000);
        conn.setRequestMethod("POST");
        conn.setDoInput(true);
        conn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(encodeParams(postDataParams));
        writer.flush();
        writer.close();
        os.close();

        int responseCode = conn.getResponseCode(); // To Check for 200
        if (responseCode == HttpsURLConnection.HTTP_OK) {

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer("");
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line);
                break;
            }
            in.close();
            return sb.toString();
        }
        return null;
    }

    private static String encodeParams(JSONObject params) throws Exception {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        Iterator<String> itr = params.keys();
        while (itr.hasNext()) {
            String key = itr.next();
            Object value = params.get(key);
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }

    private double[] getGPS() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break                 out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    l = lm.getLastKnownLocation(providers.get(i));
                }
        if (l != null) break;
    }

    double[] gps = new double[3];
    if (l != null) {
        gps[0] = l.getLatitude();
        gps[1] = l.getLongitude();
        gps[2] = l.getAccuracy();
    }
    return gps;
}

    /**
     * Is the screen of the device on.
     * @param context the context
     * @return 1 when (at least one) screen is on
     */
    private Integer isScreenOn(Context context) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            DisplayManager dm = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            Integer screenOn = 0;
            for (Display display : dm.getDisplays()) {
                if (display.getState() != Display.STATE_OFF) {
                    screenOn = 1;
                }
            }
            return screenOn;
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            //noinspection deprecation
            if(pm.isScreenOn()) return 1;
            else return 0;
        }
    }

    private Integer getRingMode(){
        AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int mode = 0;
        switch (am.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                mode = 1;
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                mode = 2;
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                mode = 3;
                break;
        }
        return mode;
    }

}