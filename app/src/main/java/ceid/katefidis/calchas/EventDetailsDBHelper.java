package ceid.katefidis.calchas;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class EventDetailsDBHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "eventdetails_db";

    // DB Entries Limit
    private static final int DATABASE_LIMIT = 500;


    public EventDetailsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create eventdetails table
        db.execSQL(EventDetails.CREATE_TABLE);


    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + EventDetails.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public long addEventDetail(String uid, long timestamp, Integer did, Integer eid, String protaseis, String chosen,  double sf, double sr, String protaseis_last_channel,
                               String location_coords, String location_accuracy, Integer screen_state, Integer ringer_mode, Integer battery_level, float ambient_light,
                               Integer connectivity,  Integer activity_type, Integer activity_confidence)
    {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` will be inserted automatically.
        // no need to add it
        values.put(EventDetails.COLUMN_UID, uid);
        values.put(EventDetails.COLUMN_TIMESTAMP, timestamp);
        values.put(EventDetails.COLUMN_DID, did);
        values.put(EventDetails.COLUMN_EID, eid);
        values.put(EventDetails.COLUMN_PROTASEIS, protaseis);
        values.put(EventDetails.COLUMN_CHOSEN, chosen);
        values.put(EventDetails.COLUMN_SF, sf);
        values.put(EventDetails.COLUMN_SR, sr);
        values.put(EventDetails.COLUMN_PROTASEIS_LAST_CHANNEL, protaseis_last_channel);
        values.put(EventDetails.COLUMN_LOCATION_COORDS, location_coords);
        values.put(EventDetails.COLUMN_LOCATION_ACCURACY, location_accuracy);
        values.put(EventDetails.COLUMN_SCREEN_STATE, screen_state);
        values.put(EventDetails.COLUMN_RINGER_MODE, ringer_mode);
        values.put(EventDetails.COLUMN_BATTERY_LEVEL, battery_level);
        values.put(EventDetails.COLUMN_AMBIENT_LIGHT, ambient_light);
        values.put(EventDetails.COLUMN_CONNECTIVITY, connectivity);
        values.put(EventDetails.COLUMN_ACTIVITY_TYPE, activity_type);
        values.put(EventDetails.COLUMN_ACTIVITY_CONFIDENCE, activity_confidence);

        // insert row
        long id = db.insert(Notification.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public ArrayList<EventDetails> getAllEventDetails() {
        ArrayList<EventDetails> eventDetails = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + EventDetails.TABLE_NAME + " ORDER BY " +
                EventDetails.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                EventDetails eventDetail = new EventDetails();
                eventDetail.setId(cursor.getInt(cursor.getColumnIndex(Notification.COLUMN_ID)));
                eventDetail.setUid(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_UID)));
                eventDetail.setTimestamp(cursor.getLong(cursor.getColumnIndex(EventDetails.COLUMN_TIMESTAMP)));
                eventDetail.setDid(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_DID)));
                eventDetail.setEid(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_EID)));
                eventDetail.setProtaseis(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_PROTASEIS)));
                eventDetail.setChosen(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_CHOSEN)));
                eventDetail.setSf(cursor.getDouble(cursor.getColumnIndex(EventDetails.COLUMN_SF)));
                eventDetail.setSr(cursor.getDouble(cursor.getColumnIndex(EventDetails.COLUMN_SR)));
                eventDetail.setProtaseis_last_channel(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_PROTASEIS_LAST_CHANNEL)));
                eventDetail.setLocation_coords(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_LOCATION_COORDS)));
                eventDetail.setLocation_accuracy(cursor.getString(cursor.getColumnIndex(EventDetails.COLUMN_LOCATION_ACCURACY)));
                eventDetail.setScreen_state(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_SCREEN_STATE)));
                eventDetail.setRinger_mode(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_RINGER_MODE)));
                eventDetail.setBattery_level(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_BATTERY_LEVEL)));
                eventDetail.setAmbient_light(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_AMBIENT_LIGHT)));
                eventDetail.setConnectivity(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_CONNECTIVITY)));
                eventDetail.setActivity_type(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_ACTIVITY_TYPE)));
                eventDetail.setActivity_confidence(cursor.getInt(cursor.getColumnIndex(EventDetails.COLUMN_ACTIVITY_CONFIDENCE)));

                eventDetails.add(eventDetail);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return nots list
        return eventDetails;
    }

    public int getEventDetailsCount() {
        String countQuery = "SELECT  * FROM " + EventDetails.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

}
