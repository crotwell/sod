package edu.sc.seis.sod.subsetter.waveFromArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfSeismogramDC.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;


import org.w3c.dom.*;
import org.apache.log4j.*;

/**
 * NullSeismogramDCLocator.java
 *
 *
 * Created: Wed Apr  2 11:52:13 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullSeismogramDCLocator
    extends AbstractSource 
    implements SodElement, SeismogramDCLocator 
 {
    public NullSeismogramDCLocator(Element element) throws Exception{
        super(element);
    }
    
    public DataCenter getSeismogramDC(EventAccessOperations event, 
                                      NetworkAccess network, 
                                      Station station, 
                                      CookieJar cookies) throws Exception{
        throw new ConfigurationException("Cannot use NullSeismogramDCLocator to get a seismogramDC. There must be another type of SeismogramDCLocator within the configuration script");
    }

} // NullSeismogramDCLocator
