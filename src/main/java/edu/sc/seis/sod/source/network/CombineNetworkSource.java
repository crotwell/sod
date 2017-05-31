package edu.sc.seis.sod.source.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.ChannelNotFound;
import edu.sc.seis.sod.model.common.QuantityImpl;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.ChannelImpl;
import edu.sc.seis.sod.model.station.Instrumentation;
import edu.sc.seis.sod.model.station.InvalidResponse;
import edu.sc.seis.sod.model.station.NetworkAttrImpl;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.model.station.StationImpl;
import edu.sc.seis.sod.source.SodSourceException;


public class CombineNetworkSource extends AbstractNetworkSource implements NetworkSource {

    public CombineNetworkSource(Element config) throws ConfigurationException {
        super(config);
        wrapped = new ArrayList<NetworkSource>();
        NodeList children = config.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if(node instanceof Element) {
                Element el = (Element)node;
                wrapped.add((NetworkSource)SodUtil.load(el, new String[] {"network"}));
            } // end of if (node instanceof Element)
        } // end of for (int i=0; i<children.getSize(); i++)
    }

    @Override
    public String getName() {
        String out = getClass().getSimpleName()+"[";
        for (NetworkSource source : wrapped) {
            out+=source.getName()+", ";
        }
        return out.substring(0, out.length()-2)+"]";
    }

    @Override
    public TimeInterval getRefreshInterval() {
        TimeInterval out = new TimeInterval(-1, UnitImpl.MILLISECOND);
        for (NetworkSource source : wrapped) {
            if (out.getValue() < 0 || out.greaterThan(source.getRefreshInterval())) {
                out = source.getRefreshInterval();
            }
        }
        return out;
    }

    
    public NetworkSource getNetworkSource(NetworkAttrImpl attr) {
        String code = NetworkIdUtil.toStringNoDates(attr);
        NetworkSource source;
        try {
            source = getSourceForCode(code);
        } catch(SodSourceException e) {
            throw new RuntimeException("Network not found: "+NetworkIdUtil.toString(attr));
        }
        return source;
    }

    @Override
    public synchronized List<? extends NetworkAttrImpl> getNetworks() throws SodSourceException {
        List<NetworkAttrImpl> out = new ArrayList<NetworkAttrImpl>();
        for (NetworkSource source : wrapped) {
            List<? extends NetworkAttrImpl> subOut = source.getNetworks();
            if (subOut != null) {
                for (NetworkAttrImpl n : subOut) {
                    String code = NetworkIdUtil.toStringNoDates(n);
                    if (! codeToSource.containsKey(code)) {
                        codeToSource.put(code, source);
                        out.add(n);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public List<? extends StationImpl> getStations(NetworkAttrImpl net) throws SodSourceException {
        NetworkSource source = getSourceForCode(NetworkIdUtil.toStringNoDates(net));
        if (source != null) {
            return source.getStations(net);
        }
        return new ArrayList<StationImpl>();
    }

    @Override
    public List<? extends ChannelImpl> getChannels(StationImpl station) throws SodSourceException {
        NetworkSource source = getSourceForCode(NetworkIdUtil.toStringNoDates(station.getId().network_id));
        if (source != null) {
            return source.getChannels(station);
        }
        return new ArrayList<ChannelImpl>();
    }

    @Override
    public QuantityImpl getSensitivity(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        NetworkSource source = getSourceForCode(NetworkIdUtil.toStringNoDates(chan.getId().network_id));
        if (source != null) {
            QuantityImpl out = source.getSensitivity(chan);
            if (out != null) {
                return out;
            }
        }
        throw new ChannelNotFound();
    }

    @Override
    public Instrumentation getInstrumentation(ChannelImpl chan) throws ChannelNotFound, InvalidResponse, SodSourceException {
        NetworkSource source = getSourceForCode(NetworkIdUtil.toStringNoDates(chan.getId().network_id));
        if (source != null) {
            Instrumentation out = source.getInstrumentation(chan);
            if (out != null) {
                return out;
            }
        }
        throw new ChannelNotFound();
    }

    synchronized NetworkSource getSourceForCode(String code) throws SodSourceException {
        if (codeToSource.containsKey(code)) {
            return codeToSource.get(code);
        } else {
            // try and find from source
            for (NetworkSource source : wrapped) {
                List<? extends NetworkAttrImpl> sublist = source.getNetworks();
                for (NetworkAttrImpl net : sublist) {
                    if (code.equals(NetworkIdUtil.toStringNoDates(net.get_id()))) {
                        codeToSource.put(code, source);
                        return source;
                    }
                }
            }
        }
        return null;
    }
    
    public void setConstraints(NetworkQueryConstraints constraints) {
        for (NetworkSource source : wrapped) {
            source.setConstraints(constraints);
        }
    }
    List<NetworkSource> wrapped;
    
    HashMap<String, NetworkSource> codeToSource = new HashMap<String, NetworkSource>();
    
}
