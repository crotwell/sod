package edu.sc.seis.sod.source.network;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.python.modules.synchronize;
import org.w3c.dom.Element;

import edu.emory.mathcs.backport.java.util.Collections;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.network.NetworkAttrImpl;
import edu.iris.Fissures.network.NetworkIdUtil;
import edu.iris.Fissures.network.StationIdUtil;
import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.fissuresUtil.cache.CacheNetworkAccess;
import edu.sc.seis.fissuresUtil.comparator.NetworkAttrComparator;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.fissuresUtil.stationxml.ChannelSensitivityBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationChannelBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationXMLToFissures;
import edu.sc.seis.seisFile.stationxml.InstrumentSensitivity;
import edu.sc.seis.seisFile.stationxml.StaMessage;
import edu.sc.seis.seisFile.stationxml.Station;
import edu.sc.seis.seisFile.stationxml.StationIterator;
import edu.sc.seis.seisFile.stationxml.StationXMLException;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;


public class StationXML implements NetworkSource {
    
    public StationXML(Element config) throws ConfigurationException {
        if (DOMHelper.hasElement(config, URL_ELEMENT)) {
            url = SodUtil.getNestedText(SodUtil.getElement(config, URL_ELEMENT));
        } else {
            throw new ConfigurationException("No <url> element found");
        }
        if(DOMHelper.hasElement(config, AbstractNetworkSource.REFRESH_ELEMENT)) {
            refreshInterval = SodUtil.loadTimeInterval(SodUtil.getElement(config, AbstractNetworkSource.REFRESH_ELEMENT));
        } else {
            refreshInterval = new TimeInterval(1, UnitImpl.FORTNIGHT);
        }
        System.out.println("StationXML: url="+url);
    }

    
    public String getDNS() {
        return url;
    }

    public String getName() {
        return this.getClass().getName();
    }

    public TimeInterval getRefreshInterval() {
        return refreshInterval;
    }

    public CacheNetworkAccess getNetwork(NetworkAttrImpl attr) {
        return new CacheNetworkAccess(null, attr);
    }

    public List<? extends CacheNetworkAccess> getNetworkByName(String name) throws NetworkNotFound {
        throw new NetworkNotFound();
    }

    public List<? extends NetworkAttrImpl> getNetworks() {
        System.out.println("StationXML: getNetworks()");
        if (networks == null) {

            try {
                parse();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
            System.out.println("StationXML: getNetworks() done parse() "+networks.size());
        }
        return Collections.unmodifiableList(networks);
    }

    public List<? extends StationImpl> getStations(NetworkId net) {
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(net));
        List<StationImpl> out = new ArrayList<StationImpl>();
        for (StationChannelBundle b : bundles) {
            out.add(b.getStation());
        }
        return out;
    }

    public List<? extends ChannelImpl> getChannels(StationImpl station) {
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(station.getNetworkAttr()));
        for (StationChannelBundle b : bundles) {
            if (StationIdUtil.areEqual(station, b.getStation())) {
                List<ChannelImpl> out = new ArrayList<ChannelImpl>();
                for (ChannelSensitivityBundle chanSens : b.getChanList()) {
                    out.add(chanSens.getChan());
                }
                return out;
            }
        }
        return new ArrayList<ChannelImpl>();
    }

    public QuantityImpl getSensitivity(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(chanId.network_id));
        for (StationChannelBundle b : bundles) {
            if (chanId.station_code.equals( b.getStation().get_code())) {
                for (ChannelSensitivityBundle chanSens : b.getChanList()) {
                    if (ChannelIdUtil.areEqual(chanId, chanSens.getChan().get_id())) {
                        return chanSens.getSensitivity();
                    }
                }
            }
        }
        throw new ChannelNotFound();
    }

    public Instrumentation getInstrumentation(ChannelId chanId) throws ChannelNotFound, InvalidResponse {
        throw new ChannelNotFound();
    }
    
    synchronized void parse() throws XMLStreamException, StationXMLException, IOException {
        networks = new ArrayList<NetworkAttrImpl>();
        // load networks via DHI service as StationXML does not have networks other than a code in stations
        NetworkFinder finder = new NetworkFinder("edu/iris/dmc", "IRIS_NetworkDC", 2);
        finder.reset();
        knownNetworks.addAll(finder.getNetworks());
        staChanMap = new HashMap<String, List<StationChannelBundle>>();
        logger.info("Parsing network from "+url);
        URL u = new URL(url);
        InputStream in  = new BufferedInputStream(u.openStream());

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader r = factory.createXMLEventReader(in);
        XMLEvent e = r.peek();
        while(! e.isStartElement()) {
            System.out.println(e);
            e = r.nextEvent(); // eat this one
            e = r.peek();  // peek at the next
        }
        StaMessage staMessage = new StaMessage(r);
        lastLoadDate = staMessage.getSentDate();
        StationIterator it = staMessage.getStations();
        while(it.hasNext()) {
            Station s = it.next();
            System.out.println("XML Station: "+s.getNetCode()+"."+s.getStaCode()+" "+s.getStationEpochs().size()+" knownnets="+knownNetworks.size());
            
            List<StationChannelBundle> bundles = StationXMLToFissures.convert(s, knownNetworks, true);
            for (StationChannelBundle b : bundles) {
                if (!networks.contains(b.getStation().getNetworkAttrImpl())) {
                    networks.add(b.getStation().getNetworkAttrImpl());
                }
                String staKey = NetworkIdUtil.toStringNoDates(b.getStation().getNetworkAttr());
                if ( ! staChanMap.containsKey(staKey)) {
                    staChanMap.put(staKey, new ArrayList<StationChannelBundle>());
                }
                staChanMap.get(staKey).add(b);
            }
        }
        System.out.println("StationXML found "+networks.size()+" networks after parse (known network="+knownNetworks.size()+")");
        logger.info("found "+networks.size()+" networks after parse (known network="+knownNetworks.size()+")");
    }
    
    List<NetworkAttrImpl> knownNetworks = new ArrayList<NetworkAttrImpl>();;
    
    List<NetworkAttrImpl> networks;
    
    Map<String, List<StationChannelBundle>> staChanMap;
    
    String url;
    
    TimeInterval refreshInterval;
    
    String lastLoadDate;
    
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(StationXML.class);
    
    public static final String URL_ELEMENT = "url";
}
