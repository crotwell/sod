/**
 * SiteGroupTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.Site;
import edu.sc.seis.sod.RunStatus;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.w3c.dom.Element;



public class SiteGroupTemplate extends Template implements GenericTemplate {
    Map siteMap = new HashMap();
    
    public SiteGroupTemplate(Element el){
        parse(el);
    }
    
    /**if this class has an template for this tag, it creates it using the
     * passed in element and returns it.  Otherwise it returns null.
     */
    protected Object getTemplate(String tag, Element el) {
        if (tag.equals("site")) return new SiteFormatter(el, this);
        return super.getTemplate(tag, el);
    }
    
    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new SiteTemplate(){
            public String getResult(Site site){
                return text;
            }
        };
    }
    
    public String getResult() {
        StringBuffer buf = new StringBuffer();
        Iterator it = siteMap.keySet().iterator();
        while(it.hasNext()){
            Site cur = (Site)it.next();
            Iterator templateIt = templates.iterator();
            while(templateIt.hasNext()){
                buf.append(((SiteTemplate)templateIt.next()).getResult(cur));
            }
        }
        return buf.toString();
    }
    
    public void change(Site site, RunStatus status){
        siteMap.put(site, status);
    }
}

