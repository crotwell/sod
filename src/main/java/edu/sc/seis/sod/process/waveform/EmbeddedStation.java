package edu.sc.seis.sod.process.waveform;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.StationImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.subsetter.station.StationSubsetter;

/**
 * @author crotwell Created on Mar 18, 2005
 */
public class EmbeddedStation implements WaveformProcess {

    public EmbeddedStation(StationSubsetter stationSubsetter) {
        this.stationSubsetter = stationSubsetter;
    }

    public EmbeddedStation(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                stationSubsetter = (StationSubsetter)SodUtil.load((Element)node,
                                                                            "station");
            }
        }
    }

    public WaveformResult process(CacheEvent event,
                                  ChannelImpl channel,
                                  RequestFilter[] original,
                                  RequestFilter[] available,
                                  LocalSeismogramImpl[] seismograms,
                                  CookieJar cookieJar) throws Exception {
        return new WaveformResult(seismograms,
                                  stationSubsetter.accept((StationImpl)channel.getStation(),
                                                          Start.getNetworkArm().getNetwork(channel.getId().network_id)));
    }

    StationSubsetter stationSubsetter;
}