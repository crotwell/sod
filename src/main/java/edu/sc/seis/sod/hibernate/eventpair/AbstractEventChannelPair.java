package edu.sc.seis.sod.hibernate.eventpair;

import edu.sc.seis.sod.LocalSeismogramArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;


public abstract class AbstractEventChannelPair extends CookieEventPair {

    protected AbstractEventChannelPair() {
    }

    public AbstractEventChannelPair(StatefulEvent event, EventStationPair esp) {
        this(event, Status.get(Stage.EVENT_CHANNEL_POPULATION, Standing.INIT), esp);
    }

    public AbstractEventChannelPair(StatefulEvent event, Status status, EventStationPair esp) {
        super(event, status);
        setEsp(esp);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public void update(Status status){
        // this is weird, but calling the setter allows hibernate to autodetect a modified object
        setStatus(status);
        updateRetries();
        if (Start.getWaveformRecipe() != null) {
            // might be null if not a real SOD run, ie unit tests or using SOD from another app
            Start.getWaveformRecipe().setStatus(this);
        }
    }

    protected void setEsp(EventStationPair esp) {
        this.esp = esp;
    }
    public EventStationPair getEsp() {
        return esp;
    }
    protected EventStationPair esp;

    public MeasurementStorage getCookieJar() {
        if (cookieJar == null) {
            cookieJar = new MeasurementStorage();
        }
        return cookieJar;
    }
    private MeasurementStorage cookieJar;
}
