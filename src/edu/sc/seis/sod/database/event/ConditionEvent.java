/**
 * StatusEvent.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.database.event;

import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.iris.Fissures.IfEvent.NoPreferredOrigin;

public class ConditionEvent extends CacheEvent{
    public ConditionEvent(CacheEvent e, EventCondition ec) throws NoPreferredOrigin{
        super(e.get_attributes(), e.get_origins(), e.get_preferred_origin());
        cond = ec;
    }

    public EventCondition getCondition(){ return cond; }

    private EventCondition cond;
}

