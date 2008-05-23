package edu.sc.seis.sod;

import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.hibernate.StatefulEvent;


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

    public int getPairId(){ return pairId; }
    
    // for hibernate
    protected void setPairId(int pairId) {
        this.pairId = pairId;
    }

    public void update(Throwable e, Status status) {
        GlobalExceptionHandler.handle(toString(), e);
        update(status);
    }

    /**
     * sets the status on this event channel pair to be status and notifies its
     * parent
     */
    public abstract void update(Status status);
    
    public int getEventDbId() { return event.getDbid(); }

    public Status getStatus(){ return status; }
    
    public void setStatus(Status status) {
        this.status = status;
        updateRetries();
    }

    public StatefulEvent getEvent(){ return event; }
    
    /** for use by hibernate */

    /** for use by hibernate */
    protected void setEvent(StatefulEvent e) {
        this.event = e;
    }


    /** for use by hibernate */
    
    private Status status;
    private StatefulEvent event;
    private int pairId;
}
