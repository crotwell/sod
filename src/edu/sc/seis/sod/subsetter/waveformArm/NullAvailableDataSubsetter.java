package edu.sc.seis.sod.subsetter.waveFormArm;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.subsetter.waveFormArm.AvailableDataSubsetter;
import edu.sc.seis.sod.CookieJar;


/**
 * Describe class <code>NullAvailableDataSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullAvailableDataSubsetter implements AvailableDataSubsetter {

    public NullAvailableDataSubsetter() {
    }

    public NullAvailableDataSubsetter(Element config) {
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param event an <code>EventAccessOperations</code> value
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param original a <code>RequestFilter[]</code> value
     * @param available a <code>RequestFilter[]</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations event,
              NetworkAccess network,
              Channel channel,
              RequestFilter[] original,
              RequestFilter[] available,
              CookieJar cookies) {

    return true;
    }

}// NullAvailableDataSubsetter
