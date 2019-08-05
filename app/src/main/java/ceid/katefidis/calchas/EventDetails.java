package ceid.katefidis.calchas;

public class EventDetails {
    String uid;
    Integer did = 0;
    Integer eid = 0;
    String protaseis;
    String chosen;
    double sf;
    double sr;
    String protaseis_last_channel;
    String location_coords;
    String location_accuracy;
    Integer screen_state;
    Integer ringer_mode;
    Integer battery_level;
    float ambient_light = 0;
    Integer connectivity;
    Integer activity_type;
    Integer activity_confidence;


    public EventDetails(String uid, String protaseis, String protaseis_last_channel)
    {
        this.uid = uid;
        this.protaseis = protaseis;
        this.protaseis_last_channel = protaseis_last_channel;
    }

}
