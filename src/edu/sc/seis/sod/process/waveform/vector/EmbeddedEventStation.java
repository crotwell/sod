package edu.sc.seis.sod.process.waveform.vector;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.ChannelGroup;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.subsetter.eventStation.EventStationSubsetter;


/**
 * @author crotwell
 * Created on Oct 23, 2005
 */
public class EmbeddedEventStation  implements WaveformVectorProcess {

    /**
     *
     */
    public EmbeddedEventStation(Element config) throws ConfigurationException{
        NodeList childNodes = config.getChildNodes();
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                eventStation =
                    (EventStationSubsetter) SodUtil.load((Element)node, "eventStation");
                break;
            }
        }
    }

    EventStationSubsetter eventStation;
    
    public WaveformVectorResult process(EventAccessOperations event,
                                        ChannelGroup channelGroup,
                                        RequestFilter[][] original,
                                        RequestFilter[][] available,
                                        LocalSeismogramImpl[][] seismograms,
                                        CookieJar cookieJar) throws Exception {
        StringTree wrapped = eventStation.accept(event, channelGroup.getVertical().my_site.my_station, cookieJar);
        WaveformVectorResult result = new WaveformVectorResult(seismograms,
                                                               new StringTreeBranch(this, wrapped.isSuccess(), wrapped));
        return result;
    }
}
