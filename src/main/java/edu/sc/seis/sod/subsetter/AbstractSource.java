package edu.sc.seis.sod.subsetter;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;

public abstract class AbstractSource{

    public AbstractSource (String dns, String name) {
        this(dns, name, -1);
    }

    public AbstractSource (String dns, String name, int retries) {
        this.dns = dns;
        this.name = name;
        retries = -1;
    }
    
    public AbstractSource (Element config, String defaultName){
        dns = SodUtil.loadText(config, "dns", "edu/iris/dmc");
        name = SodUtil.loadText(config, "name", defaultName);
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
