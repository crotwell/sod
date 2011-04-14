package edu.sc.seis.sod.source.network;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkId;
import edu.iris.Fissures.IfNetwork.NetworkNotFound;
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
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.sac.InvalidResponse;
import edu.sc.seis.fissuresUtil.stationxml.ChannelSensitivityBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationChannelBundle;
import edu.sc.seis.fissuresUtil.stationxml.StationXMLToFissures;
import edu.sc.seis.seisFile.stationxml.Network;
import edu.sc.seis.seisFile.stationxml.NetworkIterator;
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
        checkLoaded();
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
        checkLoaded();
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
        checkLoaded();
        List<StationChannelBundle> bundles = staChanMap.get(NetworkIdUtil.toStringNoDates(chanId.network_id));
        for (StationChannelBundle b : bundles) {
            if (chanId.station_code.equals( b.getStation().get_code())) {
                for (ChannelSensitivityBundle chanSens : b.getChanList()) {
                    if (ChannelIdUtil.areEqual(chanId, chanSens.getChan().get_id()) && chanSens.getSensitivity() != null) {
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
    

    synchronized void checkLoaded() {
        if (networks == null) {
            try {
                parse();
            } catch(Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    synchronized void parse() throws XMLStreamException, StationXMLException, IOException {
        networks = new ArrayList<NetworkAttrImpl>();
        // load networks via DHI service as StationXML does not have networks other than a code in stations
        NetworkFinder finder = new NetworkFinder("edu/iris/dmc", "IRIS_NetworkDC", 2);
        finder.reset();
        knownNetworks.addAll(finder.getNetworks());
        staChanMap.clear();
        logger.info("Parsing network from "+url);
        URL u = new URL(url);
        InputStream in  = new BufferedInputStream(u.openStream());

        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLEventReader r = factory.createXMLEventReader(in);
        XMLEvent e = r.peek();
        while(! e.isStartElement()) {
            e = r.nextEvent(); // eat this one
            e = r.peek();  // peek at the next
        }
        StaMessage staMessage = new StaMessage(r);
        lastLoadDate = staMessage.getSentDate();
        NetworkIterator netIt = staMessage.getNetworks();
        while (netIt.hasNext()) {
            Network net = netIt.next();
            StationIterator it = net.getStations();
            while(it.hasNext()) {
                Station s = it.next();
                try {
                    processStation(net, s);
                } catch (StationXMLException ee) {
                    logger.error("Skipping "+s.getNetCode()+"."+s.getStaCode()+" "+ ee.getMessage());
                }
            }
        }
        logger.info("found "+networks.size()+" networks after parse (known network="+knownNetworks.size()+")");
    }
    
    void processStation(Network net, Station s) throws StationXMLException {
        for (String ignore : ignoreNets) {
            if (s.getNetCode().equals(ignore)) {
            // not sure what AB network is, skip it for now
            System.err.println("WARNING: Skipping "+ignore+" network");
            return;
            }
        }
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
    
    static String[] ignoreNets = new String[] {"AB", "AI", "BN"};
    
    List<NetworkAttrImpl> knownNetworks = new ArrayList<NetworkAttrImpl>();;
    
    List<NetworkAttrImpl> networks;
    
    Map<String, List<StationChannelBundle>> staChanMap = new HashMap<String, List<StationChannelBundle>>();
    
    String url;
    
    TimeInterval refreshInterval;
    
    String lastLoadDate;
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(StationXML.class);
    
    public static final String URL_ELEMENT = "url";
}
