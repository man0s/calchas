package ceid.katefidis.calchas;

import java.io.InputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.QuickContactBadge;
import android.widget.TextView;


public class MobileArrayAdapter extends ArrayAdapter<Protasi> implements Filterable
{
    private final Context context;
    private Filter contactFilter;
    private ArrayList<Protasi> protaseis;
    private ArrayList<Protasi> originprotaseis;


    public MobileArrayAdapter(Context context, ArrayList<Protasi> protaseis)
    {
        super(context, R.layout.list_protaseis, protaseis);
        this.context = context;
        this.protaseis = protaseis;
        this.originprotaseis = protaseis;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        boolean DarkMode = settings.getBoolean("DarkMode", false);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_protaseis, parent, false);

        //set seperator view not clickable
        rowView.setClickable(false);
        rowView.setEnabled(false);

        //Pairnw to antikeimeno pou fainetai sto position
        Protasi prot = protaseis.get(position);

        //se periptwsi pou antikeimeno tou list view einai kapoios seperator
        //fernw ws row to seperator.xml
        if (prot.score == -3.0)
        {
            rowView = inflater.inflate(R.layout.seperator, parent, false);
            TextView SeperatorText = (TextView) rowView.findViewById(R.id.seperator);
            SeperatorText.setText(prot.name);
//            Typeface face = Typeface.createFromAsset(context.getAssets(),
//                    "fonts/Lobster.ttf");
//            SeperatorText.setTypeface(face);

        }
        //se periptwsi pou einai epafi fernw mono badge kai onoma
        else if (prot.score == -2.0)
        {
            rowView = inflater.inflate(R.layout.contact, parent, false);
            TextView ContactName = (TextView) rowView.findViewById(R.id.contact_name_to_list);
            if(prot.name.length() > 16)
            {
                String name = prot.name.substring(0, Math.min(prot.name.length(), 16)) + ".";
                ContactName.setText(name);
            } else ContactName.setText(prot.name);

            //if(prot.type != null)  ContactName.setText(prot.name + "|" + prot.type);

            //Gia to badge
            //QuickContactBadge eikonacode = (QuickContactBadge) rowView.findViewById(R.id.contact_photo);
            RoundedQuickContactBadge eikonacode = (RoundedQuickContactBadge) rowView.findViewById(R.id.contact_photo);
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(prot.contactID));
            eikonacode.assignContactUri(uri);
            if (prot.photo != null)
                eikonacode.setImageBitmap(prot.photo);
            else
            {
                if(DarkMode) eikonacode.setImageResource(R.drawable.account_circle_white_48dp);
                else eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
            }

            //Gia to icon
            ImageView typeIcon = rowView.findViewById(R.id.type);
            if(prot.type != null){
                if(prot.type.equals("phone")){
                    typeIcon.setImageResource(R.drawable.ic_call_24dp);
                } else if(prot.type.equals("viber")) {
                    typeIcon.setImageResource(R.drawable.ic_viber_24dp);
                } else if(prot.type.equals("whatsapp")) {
                    typeIcon.setImageResource(R.drawable.ic_whatsapp_24dp);
                } else  typeIcon.setImageResource(R.drawable.ic_sms_24dp);
            }

        }
        else
        {
            TextView textView = (TextView) rowView.findViewById(R.id.contact_name);
            TextView textView1 = (TextView) rowView.findViewById(R.id.contact_number);
            TextView datecontacted = (TextView) rowView.findViewById(R.id.datecontacted);
            TextView network = (TextView) rowView.findViewById(R.id.network);

            //Gia na efmanizetai i wra kai imerominia tis teleutaias epikoinwnias me tin protasi
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM HH:mm",Locale.getDefault());
            String dateString = formatter.format(new Date(prot.date));
            datecontacted.setText(dateString);
            network.setText(prot.network);
            if (prot.isContact)
            {
                if(prot.name.length() > 16)
                {
                    String name = prot.name.substring(0, Math.min(prot.name.length(), 16)) + ".";
                    textView.setText(name);
                } else textView.setText(prot.name);
                //if(prot.type != null)   textView.setText(prot.name + "|" + prot.type);
                textView1.setText(prot.number);
            }
            else
            {
                textView.setText(prot.number);
                //if(prot.type != null)   textView.setText(prot.name + "|" + prot.type);
                textView1.setText("");
            }

            //Gia to badge
            //QuickContactBadge eikonacode = (QuickContactBadge) rowView.findViewById(R.id.contact_photo);
            RoundedQuickContactBadge eikonacode = (RoundedQuickContactBadge) rowView.findViewById(R.id.contact_photo);
            eikonacode.assignContactFromPhone(prot.number, true);

            if (prot.photo != null)
                eikonacode.setImageBitmap(prot.photo);
            else
            {
                if(DarkMode) eikonacode.setImageResource(R.drawable.account_circle_white_48dp);
                else eikonacode.setImageResource(R.drawable.account_circle_black_48dp);
            }

            //Gia to icon
            ImageView typeIcon = rowView.findViewById(R.id.type);
            if(prot.type != null){
                if(prot.type.equals("phone")){
                    typeIcon.setImageResource(R.drawable.ic_call_24dp);
                } else if(prot.type.equals("viber")) {
                    typeIcon.setImageResource(R.drawable.ic_viber_24dp);
                } else if(prot.type.equals("whatsapp")) {
                    typeIcon.setImageResource(R.drawable.ic_whatsapp_24dp);
                } else  typeIcon.setImageResource(R.drawable.ic_sms_24dp);
            }

        }
        return rowView;
    }

    public void resetData()
    {
        protaseis = originprotaseis;
    }

    @Override
    public Filter getFilter()
    {
        if (contactFilter == null)
            contactFilter = new ProtaseisFilter();

        return contactFilter;
    }

    public Protasi getItem(int position)
    {
        return protaseis.get(position);
    }

    public int getCount()
    {
        return protaseis.size();
    }

    @SuppressLint("NewApi")
    private class ProtaseisFilter extends Filter
    {
        @Override
        protected FilterResults performFiltering(CharSequence constraint)
        {
            FilterResults results = new FilterResults();
            // We implement here the filter logic
            if (constraint == null || constraint.length() == 0)
            {
                // No filter implemented we return all the list
                results.values = protaseis;
                results.count = protaseis.size();
            }
            else
            {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean showphoto = preferences.getBoolean("showphoto", true);

                // We perform filtering operation
                ArrayList<Protasi> nProtaseis = new ArrayList<Protasi>();

                ArrayList<Protasi> mycontactlist = getAllContacts(showphoto);

                for (Protasi p : mycontactlist)
                {
                    //edw einai o tropos me ton opoio ginetai to search
                    String tocheck1 = Normalizer.normalize(p.name, Normalizer.Form.NFD);
                    String tocheck2 = Normalizer.normalize(constraint.toString(), Normalizer.Form.NFD);
                    tocheck1 = tocheck1.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    tocheck2 = tocheck2.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
                    //tocheck2 = tocheck2.replaceAll("[^\\p{ASCII}]", "");

                    //if (p.name.toUpperCase(Locale.getDefault()).startsWith(constraint.toString().toUpperCase(Locale.getDefault())))
                    //h protash den einai to seperator Recent Calls me arithmo -1
                    if (!p.number.equals("-1")){
                        if (tocheck1.toUpperCase(Locale.getDefault()).contains(tocheck2.toUpperCase(Locale.getDefault())))
                            nProtaseis.add(p);
                    }
                }

                results.values = nProtaseis;
                results.count = nProtaseis.size();

            }
            return results;
        }

        private ArrayList<Protasi> getAllContacts (boolean withphotos)
        {

            ArrayList<Protasi> mycontactlist = new ArrayList<Protasi>();

            ContentResolver cr = context.getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.HAS_PHONE_NUMBER }, ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'", null, ContactsContract.Contacts.DISPLAY_NAME);
            if (cur.getCount() > 0)
            {
                while (cur.moveToNext())
                {

                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));

                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Protasi mycalllogrecord = new Protasi("", name, 0.0, 0.0, -2.0, true, null,  id);

                    if (withphotos)
                    {
                        //an thelw kai tis photos
                        long lid = Long.parseLong(id);
                        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, lid);
                        InputStream photoInput = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), photoUri);
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

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results)
        {

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
}