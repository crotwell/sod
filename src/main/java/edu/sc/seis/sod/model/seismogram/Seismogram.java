package edu.sc.seis.sod.model.seismogram;

import java.time.Instant;

import edu.sc.seis.sod.model.common.UnitImpl;

public class Seismogram {

	protected Instant startTime;
	protected int numPoints;
	protected float sampling;
	protected UnitImpl yUnit;
	
	protected EncodedData encodedData;
	
}
