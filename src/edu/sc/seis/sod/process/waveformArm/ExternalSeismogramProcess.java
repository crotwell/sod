package edu.sc.seis.sod.process.waveFormArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.LocalSeismogram;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.process.waveFormArm.LocalSeismogramProcess;
import edu.sc.seis.sod.SodUtil;

/**
 * ExternalSeismogramProcess.java
 *
 *
 * Created: Fri Apr 12 16:25:02 2002
 *
 * @author Philip Crotwell
 * @version $Id: ExternalSeismogramProcess.java 7205 2004-02-18 20:00:14Z groves $
 */

public class ExternalSeismogramProcess
    implements LocalSeismogramProcess
{
    public ExternalSeismogramProcess (Element config)
    throws ConfigurationException {
    externalProcess = (LocalSeismogramProcess) SodUtil.loadExternal(config);
    
    }

    public LocalSeismogram[] process(EventAccessOperations event,
                     NetworkAccess network,
                     Channel channel,
                     RequestFilter[] original,
                     RequestFilter[] available,
                     LocalSeismogram[] seismograms,
                     CookieJar cookies) throws Exception{
    return externalProcess.process(event,
                       network,
                       channel,
                       original,
                       available,
                       seismograms,
                       cookies);
    }

    LocalSeismogramProcess externalProcess;
    
}// ExternalSeismogramProcess
