package edu.sc.seis.sod.subsetter.request;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.Subsetter;

/**
 * RequestSubsetter.java Created: Wed Mar 19 15:56:24 2003
 * 
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell </a>
 * @version 1.0
 */
public interface Request extends Subsetter {

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] request,
                          CookieJar cookieJar) throws Exception;
}// RequestSubsetter
