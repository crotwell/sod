package edu.sc.seis.sod.filter;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;

/**
 * WildCardIdFilter.java
 *
 *
 * Created: Thu Dec 13 21:17:47 2001
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class WildCardIdFilter 
    implements ChannelIdFilter, StationIdFilter, NetworkIdFilter {

    public WildCardIdFilter (){
	
    }

    public boolean accept(NetworkId networkId, CookieJar cookies) {
	if (net.equals("*")) {
	    return true;
	} else {
	    return false;
	}
    }

    public boolean accept(StationId id, CookieJar cookies) {
     if (sta.equals("*")) {
	 return true;
     } else {
	 return false;
     } // end of else
     
    }

    public boolean accept(ChannelId channelId, CookieJar cookies) {
	if (chan.equals("*")) {
	    return true;
	} // end of if (chan.equals("*"))
	return false;
    }

    protected String net = "*";
    protected String sta = "*";
    protected String site= "*";
    protected String chan= "*";

}// WildCardIdFilter
