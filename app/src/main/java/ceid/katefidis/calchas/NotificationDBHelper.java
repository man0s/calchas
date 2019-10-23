package ceid.katefidis.calchas;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class NotificationDBHelper extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notifications_db";

    // DB Entries Limit
    private static final int DATABASE_LIMIT = 500;


    public NotificationDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create nots table
        db.execSQL(Notification.CREATE_TABLE);
        // nots trigger for keeping the last (500) db entries
        db.execSQL("CREATE TRIGGER notificationLimit" +
                " AFTER INSERT on " + Notification.TABLE_NAME +
                " BEGIN "+
                "DELETE FROM " + Notification.TABLE_NAME + " WHERE " + Notification.COLUMN_TIMESTAMP + " <= (SELECT " + Notification.COLUMN_TIMESTAMP + " FROM " + Notification.TABLE_NAME + " ORDER BY " + Notification.COLUMN_TIMESTAMP + " DESC LIMIT " + DATABASE_LIMIT + ", 1);" +
                " END;");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Notification.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public void addNotification(long timestamp, String contact, String type) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` will be inserted automatically.
        // no need to add it
        values.put(Notification.COLUMN_TIMESTAMP, timestamp);
        values.put(Notification.COLUMN_CONTACT, contact);
        values.put(Notification.COLUMN_TYPE, type);

        // insert row
        long id = db.insert(Notification.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
    }

    public ArrayList<Notification> getAllNotifications() {
        ArrayList<Notification> notifications = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Notification.TABLE_NAME + " ORDER BY " +
                Notification.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Notification notification = new Notification();
                notification.setId(cursor.getInt(cursor.getColumnIndex(Notification.COLUMN_ID)));
                notification.setContact(cursor.getString(cursor.getColumnIndex(Notification.COLUMN_CONTACT)));
                notification.setTimestamp(cursor.getLong(cursor.getColumnIndex(Notification.COLUMN_TIMESTAMP)));
                notification.setType(cursor.getString(cursor.getColumnIndex(Notification.COLUMN_TYPE)));

                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return nots list
        return notifications;
    }

    public int getNotificationsCount() {
        String countQuery = "SELECT  * FROM " + Notification.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }


}
