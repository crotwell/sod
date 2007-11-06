/**
 * EventChannelGroupPair.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;

public class EventVectorPair {

    /** for hibernate */
    protected EventVectorPair() {}
    
    public EventVectorPair(EventChannelPair[] chanPairs) {
        if(chanPairs == null || chanPairs.length != 3) {
            throw new IllegalArgumentException("Number of EventChannelPairs must equal 3");
        }
        this.pairs = chanPairs;
    }

    public int getEventDbId() {
        return pairs[0].getEventDbId();
    }

    public Status getStatus() {
        return pairs[0].getStatus();
    }

    public CacheEvent getEvent() {
        return pairs[0].getEvent();
    }

    public CookieJar getCookieJar() {
        return pairs[0].getCookieJar();
    }

    public ChannelGroup getChannelGroup() {
        if (channels == null) {
            Channel[] chans = new Channel[pairs.length];
            for(int i = 0; i < pairs.length; i++) {
                chans[i] = pairs[i].getChannel();
            }
            this.channels = new ChannelGroup(chans);
            for(int i = 0; i < pairs.length; i++) {
                channels.addEventChannelPair(pairs[i]);
            }
        }
        return channels;
    }

    public void update(Throwable e, Status status) {
        for(int i = 0; i < pairs.length; i++) {
            pairs[i].update(e, status);
        }
    }

    public void update(Status status) {
        for(int i = 0; i < pairs.length; i++) {
            pairs[i].update(status);
        }
    }

    public boolean equals(Object obj) {
        if(obj instanceof EventVectorPair) {
            EventVectorPair other = (EventVectorPair)obj;
            for(int i = 0; i < pairs.length; i++) {
                boolean found = false;
                for(int j = i; j < pairs.length; j++) {
                    if(pairs[i].equals(other.pairs[j])) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public int hashCode() {
        int code = 1;
        for(int i = 0; i < pairs.length; i++) {
            code += 3 * pairs[i].hashCode();
        }
        return code;
    }

    public String toString() {
        String s = "ECGroup: " + getEvent() + " ";
        for(int i = 0; i < pairs.length; i++) {
            s += ChannelIdUtil.toString(pairs[i].getChannel().get_id()) + " , ";
        }
        s += " " + getStatus();
        return s;
    }

    ChannelGroup channels;

    EventChannelPair[] pairs;
	
	// hibernate
	protected int pairId;
	protected void setPairId(int pairId) {this.pairId = pairId;}
	public int getPairId() {return pairId;}
	
    protected  EventChannelPair getEcp1() { return pairs[0];}
    protected EventChannelPair getEcp2() { return pairs[1];}
    protected EventChannelPair getEcp3() { return pairs[2];}
    private void checkPairs() {
        if (pairs == null || pairs.length != 3) {
            pairs = new EventChannelPair[3];
        }
    }
    protected void setEcp1(EventChannelPair ecp) {checkPairs();pairs[0] = ecp;}
    protected void setEcp2(EventChannelPair ecp) {checkPairs();pairs[1] = ecp;}
    protected void setEcp3(EventChannelPair ecp) {checkPairs();pairs[2] = ecp;}
}