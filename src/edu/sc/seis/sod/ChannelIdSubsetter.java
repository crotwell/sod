package edu.sc.seis.sod;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;

/**
 * ChannelIdSubsetter.java
 *
 *
 * Created: Thu Dec 13 17:15:04 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public interface ChannelIdSubsetter extends Subsetter {

    public boolean accept(ChannelId channelId, CookieJar cookies);
    
}// ChannelIdSubsetter
