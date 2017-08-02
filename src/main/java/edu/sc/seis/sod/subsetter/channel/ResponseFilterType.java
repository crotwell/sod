package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Coefficients;
import edu.sc.seis.seisFile.fdsnws.stationxml.FIR;
import edu.sc.seis.seisFile.fdsnws.stationxml.PolesZeros;
import edu.sc.seis.seisFile.fdsnws.stationxml.Polynomial;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseList;
import edu.sc.seis.seisFile.fdsnws.stationxml.ResponseStage;
import edu.sc.seis.sod.DOMHelper;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;

public class ResponseFilterType extends AbstractStageSubsetter {

	public ResponseFilterType(Element config) {
		super(config);
        String type = DOMHelper.extractText(config, "type");
	}

	private String type;


    @Override
    protected StringTree accept(ResponseStage stage) {
        if ((type.equalsIgnoreCase("poleZero") || type.equalsIgnoreCase("polesZeros")) 
                && stage.getResponseItem() instanceof PolesZeros) {
            return new Pass(this);
        } else if (type.equalsIgnoreCase("fir") && stage.getResponseItem() instanceof FIR) {
            return new Pass(this);
        } else if ((type.equalsIgnoreCase("coefficient") || type.equalsIgnoreCase("coefficients")) 
                && stage.getResponseItem() instanceof Coefficients) {
            return new Pass(this);
        } else if (type.equalsIgnoreCase("polynomial") && stage.getResponseItem() instanceof Polynomial) {
            return new Pass(this);
        } else if ((type.equalsIgnoreCase("list") || type.equalsIgnoreCase("responselist")) 
                && stage.getResponseItem() instanceof ResponseList) {
            return new Pass(this);
        } else {
           return new Fail(this, "Stage not "+type); 
        }
    }
}
