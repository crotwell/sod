/**
 * StatusEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.hibernate;

import edu.iris.Fissures.IfEvent.NoPreferredOrigin;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.Status;

public class StatefulEvent extends CacheEvent{
    
    /** For use by hibernate */
    protected StatefulEvent() {}
    
    public StatefulEvent(CacheEvent e, Status stat) throws NoPreferredOrigin{
        super(e.get_attributes(), e.get_origins(), e.getPreferred());
        this.status = stat;
    }

    public Status getStatus(){ return status; }
    
    public void setStatus(Status stat){ this.status = stat; }
    
    private Status status;

}

