/**
 * SitesInStationTemplate.java
 *
 * @author Created by Philip Oliver-Paull
 */

package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Site;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.RunStatus;
import edu.sc.seis.sod.subsetter.GenericTemplate;
import edu.sc.seis.sod.subsetter.SiteGroupTemplate;
import edu.sc.seis.sod.subsetter.StationFormatter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class SitesInStationTemplate extends NetworkInfoTemplate{
    
    private Station station;
    private List siteListeners = new ArrayList();
    private Logger logger = Logger.getLogger(SitesInStationTemplate.class);
    
    public SitesInStationTemplate(Element el, String outputLocation, Station sta) throws IOException{
        super(outputLocation);
        station = sta;
        parse(el);
        write();
    }
    
    protected Object getTemplate(String tag, Element el){
        if (tag.equals("sites")){
            SiteGroupTemplate sgt = new SiteGroupTemplate(el);
            siteListeners.add(sgt);
            return sgt;
        }
        else if (tag.equals("station")){
            return new MyStationTemplate(el);
        }
        return super.getTemplate(tag,el);
    }
    
    public void change(Site site, RunStatus status){
        logger.debug("change(site, status): "  + station.my_network.get_code() + '.' + station.get_code()
                         + '.' + site.get_code() + ", " + status.toString());
        Iterator it = siteListeners.iterator();
        while (it.hasNext()){
            ((SiteGroupTemplate)it.next()).change(site, status);
        }
        try {
            write();
        } catch (IOException e) {
            CommonAccess.handleException(e, "trouble writing file");
        }
    }
    
    private class MyStationTemplate implements GenericTemplate{
        public MyStationTemplate(Element el){ formatter = new StationFormatter(el); }
        
        public String getResult(){
            return formatter.getResult(station);
        }
        
        StationFormatter formatter;
    }
}

