package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.networkArm.SiteSubsetter;
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

    public SiteCode(Element config) {
        this.code = SodUtil.getNestedText(config);
        if (code == null || code.length() == 0) {
            // site codes can be space-space and some
            // xml editors will pruge the empty space, so we take
            // the empty siteCode tag to mean space-space
            code = "  ";
        }
    }

    public boolean accept(Site site) {
        if(site.get_id().site_code.equals(code)) return true;
        else return false;
    }

    private String code;
}
