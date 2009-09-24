package edu.sc.seis.sod.subsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;

public abstract class AbstractSource{

    public AbstractSource (String dns, String name) {
        this.dns = dns;
        this.name = name;
        retries = -1;
    }
    
    public AbstractSource (Element config){
        NodeList children = config.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                String tagName  = el.getTagName();
                if(tagName.equals("dns")) dns = SodUtil.getText(el);
                else if(tagName.equals("name")) name = SodUtil.getText(el);
            }
        }
        retries = SodUtil.loadInt(config, "retries", -1);
    }
    
    /**
     * returns the DNSName of the server.
     * The context underwhich the objectName is registered in the CORBA naming service.
     *
     * @return a <code>String</code> value
     */
    public String getDNS() {
        return dns;
    }
    
    /**
     * returns the sourceName of the server. The name to which the server's servant instance is bound
     * in the CORBA naming service.
     *
     * @returns a <code>String</code> value
     */
    public String getName() {
        return name;
    }
    
    public int getRetries() {
        return retries;
    }

    public FissuresNamingService getFissuresNamingService() {
        return CommonAccess.getNameService();
    }
    
    private String name, dns;

    private int retries = -1;
    
    private static Logger logger = Logger.getLogger(AbstractSource.class);
}// AbstractSource
