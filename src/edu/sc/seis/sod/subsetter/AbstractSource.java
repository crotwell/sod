package edu.sc.seis.sod.subsetter;
import edu.sc.seis.sod.*;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractSource{
    public AbstractSource (Element config){
        NodeList children = config.getChildNodes();
        for(int i=0; i<children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                Element el = (Element)children.item(i);
                String tagName  = el.getTagName();
                if(tagName.equals("dns")) dns = SodUtil.getText(el);
                else if(tagName.equals("name"))name = SodUtil.getText(el);
            }
        }
    }
    
    /**
     * returns the DNSName of the server.
     * The context underwhich the objectName is registered in the CORBA naming service.
     *
     * @return a <code>String</code> value
     */
    public String getDNSName() {// end of for (int i=0; i<children.getSize(); i++)
        return dns;
    }
    
    /**
     * returns the sourceName of the server. The name to which the server's servant instance is bound
     * in the CORBA naming service.
     *
     * @returns a <code>String</code> value
     */
    public String getSourceName() {
        return name;
    }
    
    private String name, dns;
    
    private static Logger logger = Logger.getLogger(AbstractSource.class);
}// AbstractSource
