/**
 * RequestPrint.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.waveformArm.RequestSubsetter;
import org.w3c.dom.Element;

public class RequestPrint
    implements RequestSubsetter {

    public RequestPrint(Element config)
        throws ConfigurationException
    {

    }

    public boolean accept(EventAccessOperations event,
                          Channel channel,
                          RequestFilter[] request, CookieJar cookieJar)
        throws Exception
    {
        System.out.println("Request "+ChannelIdUtil.toString(channel.get_id())
                          +" from "+request[0].start_time.date_time
                          +" to "+request[0].end_time.date_time);
        return true;
    }
}

