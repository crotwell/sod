package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveformArm.LocalSeismogramProcess;
import org.w3c.dom.Element;

/**
 * ExternalSeismogramProcess.java
 *
 *
 * Created: Fri Apr 12 16:25:02 2002
 *
 * @author Philip Crotwell
 * @version $Id: ExternalSeismogramProcess.java 7822 2004-03-25 20:34:37Z crotwell $
 */

public class ExternalSeismogramProcess implements LocalSeismogramProcess {

    public ExternalSeismogramProcess (Element config)
        throws ConfigurationException {
        externalProcess = (LocalSeismogramProcess) SodUtil.loadExternal(config);
    }

    public LocalSeismogramImpl[] process(EventAccessOperations event,
                                         Channel channel,
                                         RequestFilter[] original,
                                         RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms, CookieJar cookieJar)
        throws Exception{
        return externalProcess.process(event, channel, original, available,
                                       seismograms, cookieJar);
    }

    LocalSeismogramProcess externalProcess;
}// ExternalSeismogramProcess
