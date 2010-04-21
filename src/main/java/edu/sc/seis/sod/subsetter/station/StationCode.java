package edu.sc.seis.sod.subsetter.station;

import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class StationCode implements StationSubsetter {

    public StationCode(Element config) throws ConfigurationException { 
        this.config = config;
        if (SodUtil.getNestedText(config).trim().length() > 5) {
            throw new ConfigurationException("Station codes are limited to 5 characters, not "+SodUtil.getNestedText(config).trim().length()+" as in '"+SodUtil.getNestedText(config).trim()+"'");
        }
        pattern = Pattern.compile(SodUtil.getNestedText(config));
    }

    public StringTree accept(StationImpl station, NetworkSource network) {
        if(pattern.matcher(station.get_code()).matches()) {
            return new Pass(this);
        } else {
            return new Fail(this);
        }
    }
    
    Pattern pattern;

    private Element config = null;
}
