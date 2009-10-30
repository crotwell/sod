package edu.sc.seis.sod.subsetter.origin;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.LogicalSubsetter;
import edu.sc.seis.sod.subsetter.Subsetter;

public class EventLogicalSubsetter extends LogicalSubsetter{
    public EventLogicalSubsetter (Element config) throws ConfigurationException{
        super(config);
    }


    public static final List<String> packages = Collections.singletonList("origin");
    
    public List<String> getPackages() {
        return packages;
    }
    
    @Override
    protected Subsetter getSubsetter(Subsetter s) throws ConfigurationException {
        return createSubsetter(s);
    }

    public static OriginSubsetter createSubsetter(Subsetter s) throws ConfigurationException {
        if (s instanceof OriginSubsetter) {
            return (OriginSubsetter)s;
        }
        throw new ConfigurationException("Subsetter of type "+s.getClass()+" cannot appear here");
    }
    
}// EventLogicalSubsetter
