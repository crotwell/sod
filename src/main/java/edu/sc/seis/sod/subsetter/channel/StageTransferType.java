package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class StageTransferType extends AbstractStageSubsetter implements
        ChannelSubsetter {

    public StageTransferType(Element config) {
        super(config);
        String type = DOMHelper.extractText(config, "type");
    }

    @Override
    protected StringTree accept(ResponseStage stage) {
        if (stage.getResponseItem() instanceof PolesZeros) {
            PolesZeros polesZeros = (PolesZeros)stage.getResponseItem();
            return new StringTreeLeaf(this, type.equals(polesZeros.getPzTransferType()));
        } else {
            return new Fail(this, "Stage "+getStageNum()+" is not PolesZeros: "+stage.getResponseItem().getClass().getSimpleName());
        }
    }
    
    String type;

}
