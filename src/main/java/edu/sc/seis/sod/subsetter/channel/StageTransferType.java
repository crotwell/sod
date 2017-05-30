package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.model.station.Stage;
import edu.sc.seis.sod.model.station.TransferType;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class StageTransferType extends AbstractStageSubsetter implements
        ChannelSubsetter {

    public StageTransferType(Element config) {
        super(config);
        String typeStr = DOMHelper.extractText(config, "type");
        if(typeStr.equalsIgnoreCase("laplace")) {
            type = TransferType.LAPLACE;
        } else if(typeStr.equalsIgnoreCase("analog")) {
            type = TransferType.ANALOG;
        } else if(typeStr.equalsIgnoreCase("composite")) {
            type = TransferType.COMPOSITE;
        } else if(typeStr.equalsIgnoreCase("digital")) {
            type = TransferType.DIGITAL;
        }
    }

    protected StringTree accept(Stage stage) {
        return new StringTreeLeaf(this, type.equals(stage.type));
    }

    TransferType type;
}
