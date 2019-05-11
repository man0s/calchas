package ceid.katefidis.calchas;

/**
 * Created by ravi on 20/02/18.
 */

public class Notification {
    public static final String TABLE_NAME = "notifications";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CONTACT = "contact";

    private int id;
    private long timestamp;
    private String contact;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TIMESTAMP + " INTEGER,"
                    + COLUMN_CONTACT + " TEXT"
                    + ")";

    public Notification() {
    }

    public Notification(int id, long timestamp, String contact) {
        this.id = id;
        this.timestamp = timestamp;
        this.contact = contact;
    }

    public int getId() {
        return id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getContact() {
        return contact;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }
}
