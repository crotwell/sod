/**
 * Choice.java
 * 
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.Tourist;

public class Choice extends AbstractMultigenitorForm {
	public Choice(int min, int max) {
		this(min, max, null);
	}

	public Choice(int min, int max, Form parent) {
		super(min, max, parent);
	}

	public FormProvider copyWithNewParent(Form newParent) {
		Choice c = new Choice(getMin(), getMax(), newParent);
		super.copyGutsOver(c);
		return c;
	}

	public void accept(Tourist v) {
		v.visit(this);
		super.accept(v);
		v.leave(this);
	}
}

