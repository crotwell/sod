package edu.sc.seis.sod.subsetter;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

public abstract class LogicalSubsetter implements Subsetter {
    
    protected LogicalSubsetter() {}
    
    public LogicalSubsetter (Element config) throws ConfigurationException {
        NodeList kids = config.getChildNodes();
        for (int i = 0; i< kids.getLength(); i++) {
            if (kids.item(i) instanceof Element) {
                Object obj = SodUtil.load((Element)kids.item(i), getPackages().toArray(new String[0]));
                if(obj instanceof Subsetter){ filterList.add(getSubsetter((Subsetter)obj)); }
            }
        }
    }

    public abstract List<String> getPackages();
    
    protected abstract Subsetter getSubsetter(Subsetter s) throws ConfigurationException;

    protected List<Subsetter> filterList = new LinkedList<Subsetter>();

    private static Logger logger = LoggerFactory.getLogger(LogicalSubsetter.class);

}// LogicalSubsetter
