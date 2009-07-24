package edu.sc.seis.sod.subsetter;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

public abstract class LogicalSubsetter implements Subsetter {
    public LogicalSubsetter (Element config) throws ConfigurationException {
        NodeList kids = config.getChildNodes();
        for (int i = 0; i< kids.getLength(); i++) {
            if (kids.item(i) instanceof Element) {
                Object obj = SodUtil.load((Element)kids.item(i), getPackage());
                if(obj instanceof Subsetter){ filterList.add(obj); }
            }
        }
    }

    public abstract String getPackage();

    protected List filterList = new LinkedList();

    private static Logger logger = Logger.getLogger(LogicalSubsetter.class);

}// LogicalSubsetter
