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
    public StatefulEvent(CacheEvent e, Status stat) throws NoPreferredOrigin{
        super(e.get_attributes(), e.get_origins(), e.get_preferred_origin());
        this.stat = stat;
    }

    public Status getStatus(){ return stat; }

    private Status stat;
}

