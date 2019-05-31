package ceid.katefidis.calchas;

public class EventDetails {
    Integer pin;
    Integer eid;
    String protaseis;
    String chosen;
    double sf;
    double sr;
    String protaseis_last_channel;


    public EventDetails(String protaseis, String protaseis_last_channel)
    {
        this.protaseis = protaseis;
        this.protaseis_last_channel = protaseis_last_channel;
    }

}
