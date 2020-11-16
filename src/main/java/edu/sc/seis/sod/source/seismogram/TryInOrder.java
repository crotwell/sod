package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;

public class TryInOrder implements SeismogramSourceLocator {

    public TryInOrder(Element config)
            throws ConfigurationException {
        NodeList childNodes = config.getChildNodes();
        Node node;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            node = childNodes.item(counter);
            if(node instanceof Element) {
                SodElement sodElement = (SodElement)SodUtil.load((Element)node,
                                                                 new String[] {"seismogram"});
                if(sodElement instanceof SeismogramSourceLocator) {
                    choices.add((SeismogramSourceLocator)sodElement);
                }
            } // end of else
        }
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        List<SeismogramSource> out = new ArrayList<SeismogramSource>();
        Iterator<SeismogramSourceLocator> it = choices.iterator();
        while(it.hasNext()) {
            out.add(it.next().getSeismogramSource(event, channel, infilters, cookieJar));
        }
        return new TryInOrderSource(out);
    }

    private List<SeismogramSourceLocator> choices = new ArrayList<SeismogramSourceLocator>();
}

class TryInOrderSource implements SeismogramSource {

    TryInOrderSource(List<SeismogramSource> sources) {
        this.sources = sources;
    }

    SeismogramSource best;

    List<SeismogramSource> sources;

    @Override
    public List<RequestFilter> availableData(List<RequestFilter> request) throws SeismogramSourceException  {
        Iterator<SeismogramSource> it = sources.iterator();
        while (it.hasNext()) {
            try {
                SeismogramSource seismogramSource = (SeismogramSource)it.next();
                List<RequestFilter> out = seismogramSource.availableData(request);
                if(out.size() > 0) {
                    best = seismogramSource;
                    return out;
                }
            } catch(org.omg.CORBA.SystemException e) {
                // Go on to next datacenter
            }
        }
        best = sources.get(sources.size()-1);
        return new ArrayList<RequestFilter>();
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
    	if(best == null) {
    		// availData hasn't been called, so fake it
    		List<RequestFilter> avail = availableData(request);
    	}
        return best.retrieveData(request);
    }
}
