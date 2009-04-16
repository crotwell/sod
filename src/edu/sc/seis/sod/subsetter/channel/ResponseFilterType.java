package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.iris.Fissures.IfNetwork.Filter;
import edu.iris.Fissures.IfNetwork.FilterType;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ResponseFilterType extends AbstractResponseFilterSubsetter {

	public ResponseFilterType(Element config) {
		super(config);
        String typeStr = DOMHelper.extractText(config, "type");
		if ( typeStr.equalsIgnoreCase("poleZero")) {
			type = FilterType.POLEZERO;
		} else if (typeStr.equalsIgnoreCase("coefficient")) {
			type = FilterType.COEFFICIENT;
		} else if (typeStr.equalsIgnoreCase("list")) {
			type = FilterType.LIST;
		}
	}

	protected StringTree accept(Filter filter) {
		return new StringTreeLeaf(this, type.value() == filter.discriminator()
				.value());
	}

	private FilterType type;
}
