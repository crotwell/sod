package edu.sc.seis.sod.subsetter;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.*;

import org.w3c.dom.*;

public class Interval implements Subsetter{

	public Interval(Element config) {
		this.config = config;		
	}

	public String getUnit() {
		return SodUtil.getNestedText(SodUtil.getElement(config,"unit"));
	}

	public String getValue() {
		return SodUtil.getNestedText(SodUtil.getElement(config,"value"));
	}

	private Element config;
}//Interval
