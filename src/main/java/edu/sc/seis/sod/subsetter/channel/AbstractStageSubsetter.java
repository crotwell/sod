package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractStageSubsetter extends AbstractResponseSubsetter {

    public AbstractStageSubsetter(Element config) {
        stageNum = DOMHelper.extractInt(config, "stage", 0);
    }

    protected StringTree accept(Response response) {
        if(response.getResponseStageList().size() > stageNum) {
            return accept(response.getResponseStageList().get(stageNum));
        }
        return new Fail(this, "stage " + stageNum + " does not exist");
    }

    protected abstract StringTree accept(ResponseStage stage);

    
    public int getStageNum() {
        return stageNum;
    }

    private int stageNum = 0;
}
