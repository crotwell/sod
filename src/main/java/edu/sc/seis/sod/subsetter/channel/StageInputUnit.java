package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Stage;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.status.StringTree;

public class StageInputUnit extends AbstractStageUnit {

    public StageInputUnit(Element config) throws ConfigurationException {
        super(config);
    }

    protected StringTree accept(Stage stage) {
        return accept(stage.input_units);
    }
    
}
