package edu.sc.seis.sod.subsetter.waveFormArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.*;


/**
 * Describe class <code>NullEventStationSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventStationSubsetter implements EventStationSubsetter {

    public NullEventStationSubsetter() {
    }
    public NullEventStationSubsetter(Element config) {
    }
    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Station</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations o, NetworkAccess networkAccess, Station station,  CookieJar cookies) {
    return true;
    }

}// NullEventStationSubsetter
