/**
 * ChannelGroupRequestSubsetterNOT.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.subsetter.request.vector;

import java.util.Iterator;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.hibernate.eventpair.MeasurementStorage;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;

public class VectorRequestNOT extends VectorRequestLogical implements
        VectorRequestSubsetter {

    public VectorRequestNOT(Element config) throws ConfigurationException {
        super(config);
    }

    public StringTree accept(CacheEvent event,
                          ChannelGroup channel,
                          RequestFilter[][] request,
                          MeasurementStorage cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        if(it.hasNext()) {
            VectorRequestSubsetter filter = (VectorRequestSubsetter)it.next();
            StringTree result = filter.accept(event, channel, request, cookieJar);
            return new StringTreeBranch(this, ! result.isSuccess(), result);
        }
        return new Pass(this);
    }
}