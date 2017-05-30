package edu.sc.seis.sod.channelGroup;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;


public class OrientedSiteRule extends SiteChannelRule {

    public OrientedSiteRule(String[] sites, char[] orientations) {
        this.siteCodes = sites;
        this.orientations = orientations;
    }

    public OrientedSiteRule(Element config) {
        NodeList childNodes = config.getChildNodes();
        int orientIndex=0;
        for(int counter = 0; counter < childNodes.getLength(); counter++) {
            Node node = childNodes.item(counter);
            if(node instanceof Element) {
                Element nodeEl = (Element)node;
                String tagName = nodeEl.getTagName();
                if (tagName.equals("orient")) {
                    siteCodes[orientIndex] = nodeEl.getAttribute("site");
                    orientations[orientIndex] = nodeEl.getAttribute("orientation").charAt(0);
                    orientIndex++;
                }
            }
        }
    }
    
    public List<ChannelGroup> acceptable(List<ChannelImpl> chanList, List<ChannelImpl> failures) {
        List<ChannelGroup> out = acceptable(null, chanList, failures);
        return out;
    }

    List<ChannelGroup> acceptable(ChannelImpl first, List<ChannelImpl> chanList, List<ChannelImpl> failures) {
        List<ChannelGroup> out = new ArrayList<ChannelGroup>();
        if (first != null) {
            ChannelImpl second = null;
            ChannelImpl third = null;
            for (ChannelImpl chan : chanList) {
                if (orientations[1] == chan.getId().channel_code.charAt(2) && siteCodes[1].equals(chan.getId().site_code)) {
                    second = chan;
                }
                if (orientations[2] == chan.getId().channel_code.charAt(2) && siteCodes[2].equals(chan.getId().site_code)) {
                    third = chan;
                }
            }
            if (second != null && third != null) {
                out.add(new ChannelGroup(new ChannelImpl[] {first, second, third}));
                chanList.remove(second);
                chanList.remove(third);
            } else {
                failures.add(first);
            }
        }
        if (chanList.size() > 2) {
            ChannelImpl nextFirst = null;
            for (ChannelImpl c : chanList) {
                if (orientations[0] == c.getId().channel_code.charAt(2) && siteCodes[0].equals(c.getId().site_code)) {
                    nextFirst = c;
                    chanList.remove(nextFirst);
                    break;
                }
            }
            if (nextFirst != null) {
                out.addAll(acceptable(nextFirst, chanList, failures));
            } else {
                failures.addAll(chanList);
                chanList.clear();
            }
        } else {
            failures.addAll(chanList);
            chanList.clear();
        }
        return out;
    }
    
    String[] siteCodes =  new String[3];
    char[] orientations = new char[3];
}
