package edu.sc.seis.sod;

import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;

/**
 * SeismogramDCLocator.java
 *
 *
 * Created: Thu Jul 25 16:19:09 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface SeismogramDCLocator {
    
    public DataCenter getSeismogramDC(EventAccessOperations event, 
                                      NetworkAccess network, 
                                      Station station, 
                                      CookieJar cookies) throws Exception;

}// SeismogramDCLocator
