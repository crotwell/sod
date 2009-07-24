package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.sc.seis.fissuresUtil.cache.InstrumentationInvalid;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.Fail;
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
        try {
            return new StringTreeLeaf(this, type.equals(stage.type));
        } catch(InstrumentationInvalid e) {
            return new Fail(this, "Invalid instrumentation");
        }
    }

    TransferType type;
}
