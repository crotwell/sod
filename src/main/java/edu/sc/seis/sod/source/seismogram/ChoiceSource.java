package edu.sc.seis.sod.source.seismogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.hibernate.eventpair.CookieJar;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.seismogram.RequestFilter;
import edu.sc.seis.sod.model.station.ChannelImpl;

/**
 * @author groves Created on May 4, 2005
 */
public class ChoiceSource implements SeismogramSourceLocator {

    public ChoiceSource(List<ChoiceSourceItem> choices, SeismogramSourceLocator otherwise) {
        this.choices = choices;
        this.otherwise = otherwise;
    }
    
    public ChoiceSource(Element config) throws ConfigurationException {
        NodeList choiceNodes = DOMHelper.extractNodes(config, "choice");
        for(int i = 0; i < choiceNodes.getLength(); i++) {
            choices.add(new ChoiceSourceItem((Element)choiceNodes.item(i)));
        }
        Element otherwiseEl = DOMHelper.extractElement(config, "otherwise/*");
        otherwise = (SeismogramSourceLocator)SodUtil.load(otherwiseEl, "seismogram");
    }

    public SeismogramSource getSeismogramSource(CacheEvent event,
                                             ChannelImpl channel,
                                             RequestFilter[] infilters,
                                             CookieJar cookieJar)
            throws Exception {
        Iterator<ChoiceSourceItem> it = choices.iterator();
        while(it.hasNext()) {
            ChoiceSourceItem cur = it.next();
            if(cur.accept(event, channel, cookieJar).isSuccess()) {
                return cur.getSeismogramSource(event, channel, infilters, cookieJar);
            }
        }
        return otherwise.getSeismogramSource(event, channel, infilters, cookieJar);
    }

    private List<ChoiceSourceItem> choices = new ArrayList<ChoiceSourceItem>();

    private SeismogramSourceLocator otherwise;
}
