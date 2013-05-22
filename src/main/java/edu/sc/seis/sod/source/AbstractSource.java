package edu.sc.seis.sod.source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.namingService.FissuresNamingService;
import edu.sc.seis.sod.CommonAccess;
import edu.sc.seis.sod.SodUtil;

public abstract class AbstractSource implements Source{

    public AbstractSource (String name) {
        this(name, -1);
    }

    public AbstractSource (String name, int retries) {
        this.name = name;
        retries = -1;
    }
    
    public AbstractSource (Element config, String defaultName){
        name = SodUtil.loadText(config, "name", defaultName);
        retries = SodUtil.loadInt(config, "retries", -1);
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
    
    protected String name;

    private int retries = -1;
    
    private static Logger logger = LoggerFactory.getLogger(AbstractSource.class);
}// AbstractSource
