package ceid.katefidis.calchas;

public class EventDetails {
    public static final String TABLE_NAME = "eventdetails";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_UID = "uid";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final String COLUMN_DID = "did";
    public static final String COLUMN_EID = "eid";
    public static final String COLUMN_PROTASEIS = "protaseis";
    public static final String COLUMN_CHOSEN = "chosen";
    public static final String COLUMN_SF = "sf";
    public static final String COLUMN_SR = "sr";
    public static final String COLUMN_PROTASEIS_LAST_CHANNEL = "protaseis_last_channel";
    public static final String COLUMN_LOCATION_COORDS = "location_coords";
    public static final String COLUMN_LOCATION_ACCURACY = "location_accuracy";
    public static final String COLUMN_SCREEN_STATE = "screen_state";
    public static final String COLUMN_RINGER_MODE = "ringer_mode";
    public static final String COLUMN_BATTERY_LEVEL = "battery_level";
    public static final String COLUMN_AMBIENT_LIGHT = "ambient_light";
    public static final String COLUMN_CONNECTIVITY = "connectivity";
    public static final String COLUMN_ACTIVITY_TYPE = "activity_type";
    public static final String COLUMN_ACTIVITY_CONFIDENCE = "activity_confidence";

    private Integer id;
    private String uid;
    private long timestamp;
    private Integer did = 0;
    private Integer eid = 0;
    private String protaseis;
    private String chosen;
    private double sf;
    private double sr;
    private String protaseis_last_channel;
    private String location_coords;
    private String location_accuracy;
    private Integer screen_state;
    private Integer ringer_mode;
    private Integer battery_level;
    private float ambient_light = 0;
    private Integer connectivity;
    private Integer activity_type;
    private Integer activity_confidence;


    public EventDetails() { }

    public EventDetails(long timestamp, String uid, String protaseis, String protaseis_last_channel)
    {
        this.timestamp = timestamp;
        this.uid = uid;
        this.protaseis = protaseis;
        this.protaseis_last_channel = protaseis_last_channel;
    }


    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_UID + " "
                    + COLUMN_TIMESTAMP + " INTEGER,"
                    + COLUMN_DID + " "
                    + COLUMN_EID + " "
                    + COLUMN_PROTASEIS + " "
                    + COLUMN_CHOSEN + " "
                    + COLUMN_SF + " "
                    + COLUMN_SR + " "
                    + COLUMN_PROTASEIS_LAST_CHANNEL + " "
                    + COLUMN_LOCATION_COORDS + " "
                    + COLUMN_LOCATION_ACCURACY + " "
                    + COLUMN_SCREEN_STATE + " "
                    + COLUMN_RINGER_MODE + " "
                    + COLUMN_BATTERY_LEVEL + " "
                    + COLUMN_AMBIENT_LIGHT + " "
                    + COLUMN_CONNECTIVITY + " "
                    + COLUMN_ACTIVITY_TYPE + " "
                    + COLUMN_ACTIVITY_CONFIDENCE + " "
                    + ")";

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getDid() {
        return did;
    }

    public void setDid(Integer did) {
        this.did = did;
    }

    public Integer getEid() {
        return eid;
    }

    public void setEid(Integer eid) {
        this.eid = eid;
    }

    public String getProtaseis() {
        return protaseis;
    }

    public void setProtaseis(String protaseis) {
        this.protaseis = protaseis;
    }

    public String getChosen() {
        return chosen;
    }

    public void setChosen(String chosen) {
        this.chosen = chosen;
    }

    public double getSf() {
        return sf;
    }

    public void setSf(double sf) {
        this.sf = sf;
    }

    public double getSr() {
        return sr;
    }

    public void setSr(double sr) {
        this.sr = sr;
    }

    public String getProtaseis_last_channel() {
        return protaseis_last_channel;
    }

    public void setProtaseis_last_channel(String protaseis_last_channel) {
        this.protaseis_last_channel = protaseis_last_channel;
    }

    public String getLocation_coords() {
        return location_coords;
    }

    public void setLocation_coords(String location_coords) {
        this.location_coords = location_coords;
    }

    public String getLocation_accuracy() {
        return location_accuracy;
    }

    public void setLocation_accuracy(String location_accuracy) {
        this.location_accuracy = location_accuracy;
    }

    public Integer getScreen_state() {
        return screen_state;
    }

    public void setScreen_state(Integer screen_state) {
        this.screen_state = screen_state;
    }

    public Integer getRinger_mode() {
        return ringer_mode;
    }

    public void setRinger_mode(Integer ringer_mode) {
        this.ringer_mode = ringer_mode;
    }

    public Integer getBattery_level() {
        return battery_level;
    }

    public void setBattery_level(Integer battery_level) {
        this.battery_level = battery_level;
    }

    public float getAmbient_light() {
        return ambient_light;
    }

    public void setAmbient_light(float ambient_light) {
        this.ambient_light = ambient_light;
    }

    public Integer getConnectivity() {
        return connectivity;
    }

    public void setConnectivity(Integer connectivity) {
        this.connectivity = connectivity;
    }

    public Integer getActivity_type() {
        return activity_type;
    }

    public void setActivity_type(Integer activity_type) {
        this.activity_type = activity_type;
    }

    public Integer getActivity_confidence() {
        return activity_confidence;
    }

    public void setActivity_confidence(Integer activity_confidence) {
        this.activity_confidence = activity_confidence;
    }
}
