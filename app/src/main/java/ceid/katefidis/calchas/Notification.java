package ceid.katefidis.calchas;

/**
 * Created by ravi on 20/02/18.
 */

public class Notification {
    public static final String TABLE_NAME = "notifications";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_CONTACT = "contact";
    public static final String COLUMN_TYPE = "type";

    private int id;
    private long timestamp;
    private String contact;
    private String type;


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TIMESTAMP + " INTEGER,"
                    + COLUMN_CONTACT + " TEXT,"
                    + COLUMN_TYPE + " TEXT"
                    + ")";

    public Notification() {
    }

    public Notification(int id, long timestamp, String contact, String type) {
        this.id = id;
        this.timestamp = timestamp;
        this.contact = contact;
        this.type = type;
    }


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) { this.timestamp = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
