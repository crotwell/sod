package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.Station;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.subsetter.AbstractPrintlineProcess;

public class PrintlineStationProcess extends AbstractPrintlineProcess implements
        StationSubsetter {

    public PrintlineStationProcess(Element config)
            throws ConfigurationException {
        super(config);
    }

    public boolean accept(Station station, NetworkAccess network)
            throws Exception {
        velocitizer.evaluate(filename, template, station);
        return true;
    }

    public String getDefaultTemplate() {
        return "Station: $station";
    }
}
