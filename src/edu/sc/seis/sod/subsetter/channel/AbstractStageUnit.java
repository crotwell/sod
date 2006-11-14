package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;
import edu.iris.Fissures.Unit;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public abstract class AbstractStageUnit extends AbstractStageSubsetter {

    public AbstractStageUnit(Element config) throws ConfigurationException {
        super(config);
        unit = SodUtil.loadUnit(SodUtil.getElement(config, "unit"));
    }

    protected abstract StringTree accept(Stage stage);
    
    protected StringTree accept(Unit stageUnit) {
        return new StringTreeLeaf(this,
                                  unit.isConvertableTo(UnitImpl.createUnitImpl(stageUnit))
                                          && unit.toString()
                                                  .equalsIgnoreCase(stageUnit.toString()));
    }

    UnitImpl unit;
}
