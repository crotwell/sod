package edu.sc.seis.sod.subsetter.network;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Network;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class NetworkCode implements NetworkSubsetter {

    public NetworkCode(Element config) throws ConfigurationException {
        this.desiredCode = SodUtil.getText(config);
        if(!ANY_NET.matcher(desiredCode).matches()) {
            throw new ConfigurationException("Code '"+desiredCode+"' does not look like a network code, valid examples are G, IU and YJ07");
        }
        Matcher m = TEMP_NET.matcher(desiredCode);
        if(m.matches()) {
            desiredCode = m.group(1);
            year = m.group(2);
        }
    }

    public StringTree accept(Network attr) throws Exception {
        if(attr.getCode().equals(desiredCode)
                && (year == null ||
                        (year.length() == 2 && year.equals(NetworkIdUtil.getTwoCharYear(attr))) ||
                        (year.length() == 4 && year.equals(NetworkIdUtil.getYear(attr))))) {
            return new Pass(this);
        }
        return new Fail(this);
    }

    public String getCode() {
        return desiredCode;
    }

    private String desiredCode;

    private String year;

    private static final Pattern TEMP_NET = Pattern.compile("([1-9XYZ][0-9A-Z])(\\d{2})");
    private static final Pattern ANY_NET = Pattern.compile("([0-9A-Z][0-9A-Z]?)(\\d{2})?");
}