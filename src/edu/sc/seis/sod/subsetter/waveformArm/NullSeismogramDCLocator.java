package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.waveformArm.SeismogramDCLocator;
import edu.sc.seis.sod.SodElement;
import org.w3c.dom.Element;

/**
 * NullSeismogramDCLocator.java
 *
 *
 * Created: Wed Apr  2 11:52:13 2003
 *
 * @author <a href="mailto:crotwell@owl.seis.sc.edu">Philip Crotwell</a>
 * @version 1.0
 */
public class NullSeismogramDCLocator implements SodElement, SeismogramDCLocator{
    public NullSeismogramDCLocator() {}

    public NullSeismogramDCLocator(Element element) throws Exception{}

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Station station) throws Exception{
        throw new ConfigurationException("Cannot use NullSeismogramDCLocator to get a seismogramDC. There must be another type of SeismogramDCLocator within the configuration script");
    }
} // NullSeismogramDCLocator
