/**
 * ChannelGroup.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.network.ChannelIdUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ChannelGroup {
    public ChannelGroup(Channel[] channels) {
        this.channels = channels;
    }

    public Channel[] getChannels() {
        return channels;
    }

    public boolean contains(Channel c){
        for (int i = 0; i < channels.length; i++) {
            if(channels[i].equals(c)){ return true; }
        }
        return false;
    }

    /** Attempts to group channels in to groups that are components of
     * motion. Channels that cannot be matched are put into the failures
     * List. */
    public static ChannelGroup[] group(Channel[] channels, List failures) {
        // this is a placeholder implementation until we implement the
        // full grouping xml config file idea

        // first sort by band and gain, ie all but last char of channel code
        HashMap bandGain = new HashMap();
        for (int i = 0; i < channels.length; i++) {
            MicroSecondDate msd = new MicroSecondDate(channels[i].get_id().begin_time);
            // key is channel begin time + netCode.staCode.siteCode.bandgain
            String key = ChannelIdUtil.toStringNoDates(channels[i].get_id());
            key = key.substring(0, key.length()-1);
            key = msd+key;
            LinkedList chans = (LinkedList)bandGain.get(key);
            if (chans == null) {
                chans = new LinkedList();
                bandGain.put(key, chans);
            }
            chans.add(channels[i]);
        }
        LinkedList out = new LinkedList();
        Iterator it = bandGain.keySet().iterator();
        while (it.hasNext()) {
            LinkedList chans = (LinkedList)bandGain.get(it.next());
            if (chans.size() == 3) {
                out.add(new ChannelGroup((Channel[])chans.toArray(new Channel[3])));
            } else {
                failures.addAll(chans);
            }
        }
        return (ChannelGroup[])out.toArray(new ChannelGroup[0]);
    }

    private Channel[] channels;

}

