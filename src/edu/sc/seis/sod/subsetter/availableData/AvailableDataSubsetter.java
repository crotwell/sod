package edu.sc.seis.sod.subsetter.availableData;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
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

    public StringTree accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] original,
                          RequestFilter[] available,
                          CookieJar cookieJar) throws Exception;
}// AvailableDataSubsetter
