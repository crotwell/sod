package edu.sc.seis.sod;

import edu.iris.Fissures.IfSeismogramDC.*;

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
    
    public DataCenter getSeismogramDC() throws Exception;

}// SeismogramDCLocator
