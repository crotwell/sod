package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.model.station.Filter;
import edu.sc.seis.sod.model.station.Stage;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.StringTree;

public abstract class AbstractResponseFilterSubsetter extends
		AbstractStageSubsetter {

	public AbstractResponseFilterSubsetter(Element config) {
		super(config);
		filterNum = DOMHelper.extractInt(config, "filter", 0);
	}

	protected StringTree accept(Stage stage) {
		if (stage.filters.length > filterNum) {
			return accept(stage.filters[filterNum]);
		}
		return new Fail(this, "Filter " + filterNum + " does not exist");
	}

	protected abstract StringTree accept(Filter filter);

	private int filterNum = 0;
}
