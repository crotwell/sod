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
 * Describe class <code>NullEventChannelSubsetter</code> here.
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class NullEventChannelSubsetter 
    
    implements EventChannelSubsetter {
    /**
     * Describe <code>accept</code> method here.
     *
     * @param o an <code>EventAccessOperations</code> value
     * @param networkAccess a <code>NetworkAccess</code> value
     * @param station a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(EventAccessOperations o, NetworkAccess networkAccess, Channel channel,  CookieJar cookies) {
	return true;
    }

}// NullEventChannelSubsetter
