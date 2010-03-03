package edu.sc.seis.sod.server;

import java.util.List;

import edu.sc.seis.cormorant.event.EventDataAccess;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.database.NotFound;
import edu.sc.seis.fissuresUtil.flow.querier.EventFinderQuery;
import edu.sc.seis.sod.hibernate.StatefulEvent;
import edu.sc.seis.sod.hibernate.StatefulEventDB;


public class StatefulEventDBDataAccess implements EventDataAccess {

    public StatefulEventDBDataAccess(StatefulEventDB edb) {
        this.edb = edb;
    }

    public CacheEvent[] getByName(String name) {
        return edb.getByName(name);
    }
    
    public String[] getCatalogs() {
        return edb.getCatalogs();
    }

    public String[] getCatalogsFor(String contributor) {
        return edb.getCatalogsFor(contributor);
    }

    public String[] getContributors() {
        return edb.getContributors();
    }

    public CacheEvent getEvent(int dbid) throws NotFound {
        return edb.getEvent(dbid);
    }

    public int[] query(EventFinderQuery q) {
        List<StatefulEvent> events = edb.query(q);
        int[] out = new int[events.size()];
        for(int i = 0; i < out.length; i++) {
            out[i] = events.get(i).getDbid();
        }
        return out;
    }
    
    StatefulEventDB edb;
}
