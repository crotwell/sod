package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.station.Stage;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public abstract class AbstractStageUnit extends AbstractStageSubsetter {

    public AbstractStageUnit(Element config) throws ConfigurationException {
        super(config);
        unit = SodUtil.loadUnit(SodUtil.getElement(config, "unit"));
    }

    protected abstract StringTree accept(Stage stage);
    
    protected StringTree accept(UnitImpl stageUnit) {
        return new StringTreeLeaf(this,
                                  unit.isConvertableTo(UnitImpl.createUnitImpl(stageUnit))
                                          && unit.toString()
                                                  .equalsIgnoreCase(stageUnit.toString()));
    }

    UnitImpl unit;
}
