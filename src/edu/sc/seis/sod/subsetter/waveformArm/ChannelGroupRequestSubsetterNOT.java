/**
 * ChannelGroupRequestSubsetterNOT.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter.waveformArm;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import java.util.Iterator;
import org.w3c.dom.Element;



public class ChannelGroupRequestSubsetterNOT extends WaveformLogicalSubsetter
    implements ChannelGroupRequestSubsetter {

    public ChannelGroupRequestSubsetterNOT(Element config) throws ConfigurationException{
        super(config);
    }

    public boolean accept(EventAccessOperations event, ChannelGroup channel, RequestFilter[][] request, CookieJar cookieJar) throws Exception {
        Iterator it = filterList.iterator();
        while (it.hasNext()) {
            ChannelGroupRequestSubsetter filter = (ChannelGroupRequestSubsetter)it.next();
            if (filter.accept(event, channel, request, cookieJar)) {
                return false;
            }
        }
        return true;
    }

}

