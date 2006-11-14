package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Response;
import edu.iris.Fissures.IfNetwork.Stage;
import edu.iris.Fissures.IfNetwork.TransferType;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractStageSubsetter extends AbstractResponseSubsetter {

	public AbstractStageSubsetter(Element config) {
		super(config);
		stageNum = SodUtil.loadInt(config, "stage", 0);
	}

	protected StringTree accept(Response response) {
		if (response.stages.length > stageNum) {
			return accept(response.stages[stageNum]);
		}
		return new Fail(this, "stage " + stageNum + " does not exist");
	}

	protected abstract StringTree accept(Stage stage);

	int stageNum = 0;

	TransferType type;
}
