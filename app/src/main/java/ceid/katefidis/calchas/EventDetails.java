package ceid.katefidis.calchas;

public class EventDetails {
    String uid;
    Integer did;
    Integer eid;
    String protaseis;
    String chosen;
    double sf;
    double sr;
    String protaseis_last_channel;
    String location_coords;
    String location_accuracy


    public EventDetails(String uid, String protaseis, String protaseis_last_channel)
    {
        this.uid = uid;
        this.protaseis = protaseis;
        this.protaseis_last_channel = protaseis_last_channel;
    }

}
