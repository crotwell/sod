package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.fissuresUtil.cache.ProxySeismogramDC;

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

    public ProxySeismogramDC getSeismogramDC(EventAccessOperations event,
                                             Channel channel,
                                             RequestFilter[] infilters) throws Exception;

}// SeismogramDCLocator
