package edu.sc.seis.sod.subsetter.availableData;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelImpl;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.subsetter.availableData.vector.VectorAvailableDataSubsetter;

/**
 * Describe class <code>NullAvailableDataSubsetter</code> here.
 * 
 * @author <a href="mailto:">Srinivasa Telukutla </a>
 * @version 1.0
 */
public class PassAvailableData implements AvailableDataSubsetter,
        VectorAvailableDataSubsetter {

    public PassAvailableData() {}

    public PassAvailableData(Element config) {}

    public StringTree accept(CacheEvent event,
                             ChannelImpl channel,
                             RequestFilter[] original,
                             RequestFilter[] available,
                             CookieJar cookieJar) {
        return new StringTreeLeaf(this, true);
    }

    public StringTree accept(CacheEvent event,
                             ChannelGroup channelGroup,
                             RequestFilter[][] original,
                             RequestFilter[][] available,
                             CookieJar cookieJar) throws Exception {
        return new StringTreeLeaf(this, true);
    }
}// PassAvailableData
