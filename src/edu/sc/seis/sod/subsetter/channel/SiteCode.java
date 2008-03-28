package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

/**
 * @author Srinivasa Telukutla
 */
public class SiteCode implements ChannelSubsetter {

    public SiteCode(Element config) {
        this.code = SodUtil.getNestedText(config);
        if(code == null || code.length() == 0) {
            // site codes can be space-space and some
            // xml editors will prune the empty space, so we take
            // the empty siteCode tag to mean space-space
            code = "  ";
        }
    }

    public StringTree accept(Channel chan, ProxyNetworkAccess network) {
        return new StringTreeLeaf(this, chan.my_site.get_id().site_code.equals(code));
    }

    private String code;
}
