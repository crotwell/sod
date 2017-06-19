package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public class StageOutputUnit extends AbstractStageUnit {

    public StageOutputUnit(Element config) throws ConfigurationException {
        super(config);
    }

    protected StringTree accept(ResponseStage stage) {
        return accept(stage.getResponseItem().getOutputUnits());
    }
}
