/**
 * StatusEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.event;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.Status;

public class StatefulEvent extends CacheEvent{
    
    /** For use by hibernate */
    protected StatefulEvent() {}
    
    public StatefulEvent(CacheEvent e, Status stat) throws NoPreferredOrigin{
        super(e.get_attributes(), e.get_origins(), e.get_preferred_origin());
        this.stat = stat;
    }

    public Status getStatus(){ return stat; }
    
    public void setStatus(Status stat){ setStatusAsShort(stat.getAsShort()); }

    /** for use by hibernate */
    protected short getStatusAsShort() {
        return stat.getAsShort();
    }
    
    protected void setStatusAsShort(short status) {
        stat = Status.getFromShort(status);
    }
    private Status stat;

    /** For use by hibernate */
    private Long id; 

    public Long getId() { 
        return this.id; 
    } 
    private void setId(Long id) { 
        this.id = id; 
    } 

}

