package edu.sc.seis.sod.subsetter.site;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.subsetter.site.SiteSubsetter;
import org.w3c.dom.Element;

/**
 * @author Srinivasa Telukutla
 */
public class SiteCode implements SiteSubsetter {

    public SiteCode(Element config) {
        this.code = SodUtil.getNestedText(config);
        if(code == null || code.length() == 0) {
            // site codes can be space-space and some
            // xml editors will prune the empty space, so we take
            // the empty siteCode tag to mean space-space
            code = "  ";
        }
    }

    public boolean accept(Site site) {
        return site.get_id().site_code.equals(code);
    }

    private String code;
}
