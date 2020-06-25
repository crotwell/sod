package edu.sc.seis.sod.hibernate.eventpair;

import edu.sc.seis.sod.model.event.StatefulEvent;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;


public abstract class AbstractEventPair extends WaveformWorkUnit {
    
    /** for hibernate */
    protected AbstractEventPair() {}
    
    public AbstractEventPair(StatefulEvent event) {
        this(event, null);
    }

    public AbstractEventPair(StatefulEvent event, Status status) {
        this.event = event;
        this.status = status;
    }


    public void update(Throwable e, Status status) {
        String s = "";
        try {
            s=toString();
        } catch(Throwable t) {
        }
        GlobalExceptionHandler.handle(s, e);
        update(status);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public abstract void update(Status status);
    
    public int getEventDbId() { return event.getDbid(); }

    // for hibernate
    public Status getStatus(){ return status; }
    
    protected void setStatus(Status status) {
        this.status = status;
        updateRetries();
    }

    public StatefulEvent getEvent(){ return event; }

    /** for use by hibernate */
    protected void setEvent(StatefulEvent e) {
        this.event = e;
    }

    private Status status;
    private StatefulEvent event;
}
