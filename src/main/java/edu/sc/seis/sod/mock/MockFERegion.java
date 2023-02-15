package edu.sc.seis.sod.mock;

import edu.sc.seis.sod.model.event.FlinnEngdahlRegion;
import edu.sc.seis.sod.model.event.FlinnEngdahlType;

public class MockFERegion{
    public static FlinnEngdahlRegion create(){ return create(1); }
    
    public static FlinnEngdahlRegion create(int region){
        return new FlinnEngdahlRegion(FlinnEngdahlType.from_int(1), region);
    }
}
