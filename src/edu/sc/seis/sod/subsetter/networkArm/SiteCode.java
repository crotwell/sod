package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.subsetter.networkArm.SiteSubsetter;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;

/**
 *
 * sample xml file
 * <pre>
 * &lt;siteCode&gt;&lt;value&gt;00&lt;/value&gt;&lt;/siteCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class SiteCode implements SiteSubsetter {

    /**
     * Creates a new <code>SiteCode</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public SiteCode(Element config) {
        this.config = config;
        this.code = SodUtil.getNestedText(config);
        if (code == null || code.length() == 0) {
            // site codes can be space-space and some
            // xml editors will pruge the empty space, so we take
            // the empty siteCode tag to mean space-space
            code = "  ";
        }
    }

    /**
     * Describe <code>accept</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param station a <code>Site</code> value
     * @param cookies a <code>CookieJar</code> value
     * @return a <code>boolean</code> value
     */
    public boolean accept(NetworkAccess network, Site site, CookieJar cookies) {
        if(site.get_id().site_code.equals(code)) return true;
        else return false;

    }

    private Element config = null;

    private String code;
}
