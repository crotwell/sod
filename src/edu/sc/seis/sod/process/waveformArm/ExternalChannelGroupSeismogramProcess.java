/**
 * ExternalChannelGroupSeismogramProcess.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.process.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

public class ExternalChannelGroupSeismogramProcess implements ChannelGroupLocalSeismogramProcess {

    public ExternalChannelGroupSeismogramProcess (Element config)
        throws ConfigurationException {
        externalProcess = (ChannelGroupLocalSeismogramProcess) SodUtil.loadExternal(config);
    }

    public ChannelGroupLocalSeismogramResult process(EventAccessOperations event,
                                           ChannelGroup channel,
                                           RequestFilter[][] original,
                                           RequestFilter[][] available,
                                           LocalSeismogramImpl[][] seismograms,
                                           CookieJar cookieJar)
        throws Exception{
        return externalProcess.process(event,
                                       channel,
                                       original,
                                       available,
                                       seismograms,
                                       cookieJar);
    }

    ChannelGroupLocalSeismogramProcess externalProcess;
}

