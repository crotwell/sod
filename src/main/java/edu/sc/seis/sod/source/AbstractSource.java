package edu.sc.seis.sod.source;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;

public abstract class AbstractSource implements Source{

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
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.Source#getDNS()
     */
    @Override
    public String getDNS() {
        return dns;
    }
    
    /* (non-Javadoc)
     * @see edu.sc.seis.sod.source.Source#getName()
     */
    @Override
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
