package edu.sc.seis.sod.channelGroup;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.model.station.ChannelGroup;
import edu.sc.seis.sod.model.station.ChannelImpl;

public class SiteMatchRule extends SiteChannelRule {

    public SiteMatchRule(String orientationCodes) {
        codes = orientationCodes.trim().toCharArray();
    }
    
    public SiteMatchRule(Element el) {
        this(el.getAttribute("orientations"));
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
                if (codes[1] == chan.getId().channel_code.charAt(2)) {
                    second = chan;
                }
                if (codes[2] == chan.getId().channel_code.charAt(2)) {
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
                if (codes[0] == c.get_id().channel_code.charAt(2)) {
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

    public char[] codes;
}
