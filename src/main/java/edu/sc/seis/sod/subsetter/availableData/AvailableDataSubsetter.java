package edu.sc.seis.sod.subsetter.availableData;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * AvailableDataSubsetter.java Created: Thu Dec 13 17:18:32 2001
 * 
 * @author <a href="mailto:">Philip Crotwell </a>
 * @version
 */
public interface AvailableDataSubsetter extends Subsetter {

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception;
}// AvailableDataSubsetter
