package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodElement;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;

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

    void checkBestSet() {
        if(best == null) {
            throw new RuntimeException("Cannot call method until availableData is called to pick the best DC");
        }
    }

    @Override
    public List<LocalSeismogramImpl> retrieveData(List<RequestFilter> request) throws SeismogramSourceException {
        checkBestSet();
        return best.retrieveData(request);
    }
}
