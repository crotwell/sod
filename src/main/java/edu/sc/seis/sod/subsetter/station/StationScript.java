package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.source.network.VelocityNetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;
import edu.sc.seis.sod.velocity.network.VelocityStation;


public class StationScript extends AbstractScriptSubsetter implements StationSubsetter {

    public StationScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(Station station, NetworkSource network) throws Exception {
        return runScript(new VelocityStation(station), new VelocityNetworkSource(network));
    }

    /** Run the script with the arguments as predefined variables. */
    public StringTree runScript(VelocityStation station, VelocityNetworkSource networkSource) throws Exception {
        engine.put("station", station);
        engine.put("networkSource", networkSource);
        return eval();
    }
}
