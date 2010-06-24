package edu.sc.seis.sod.subsetter.station;

import org.w3c.dom.Element;

import edu.iris.Fissures.network.StationImpl;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.AbstractScriptSubsetter;


public class StationScript extends AbstractScriptSubsetter implements StationSubsetter {

    public StationScript(Element config) {
        super(config);
    }

    @Override
    public StringTree accept(StationImpl station, NetworkSource network) throws Exception {
        engine.put("station", station);
        engine.put("network", network);
        return eval();
    }
}
