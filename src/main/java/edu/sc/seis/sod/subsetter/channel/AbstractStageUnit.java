package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.seisFile.fdsnws.stationxml.Unit;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.common.UnknownUnit;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.util.convert.stationxml.StationXMLToFissures;

public abstract class AbstractStageUnit extends AbstractStageSubsetter {

    public AbstractStageUnit(Element config) throws ConfigurationException {
        super(config);
        unit = SodUtil.loadUnit(SodUtil.getElement(config, "unit"));
    }

    protected abstract StringTree accept(ResponseStage stage);
    
    protected StringTree accept(Unit stageUnit) {
        try {
            return accept(StationXMLToFissures.convertUnit(stageUnit));
        } catch(UnknownUnit e) {
            return new Fail(this, "Unable to convert unit: ", e);
        }
    }
    
    protected StringTree accept(UnitImpl stageUnit) {
        return new StringTreeLeaf(this,
                                  unit.isConvertableTo(UnitImpl.createUnitImpl(stageUnit))
                                          && unit.toString()
                                                  .equalsIgnoreCase(stageUnit.toString()));
    }

    UnitImpl unit;
}
