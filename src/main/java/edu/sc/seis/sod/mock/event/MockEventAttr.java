package edu.sc.seis.sod.mock.event;

import edu.sc.seis.sod.mock.MockFERegion;
import edu.sc.seis.sod.mock.MockParameterRef;
import edu.sc.seis.sod.model.common.ParameterRef;
import edu.sc.seis.sod.model.event.EventAttrImpl;

public class MockEventAttr{
    public static EventAttrImpl create(){ return create(1); }
    
    public static EventAttrImpl create(int feRegion){
        return create("Test Event", feRegion);
    }
    
    public static EventAttrImpl create(String name, int feRegion){
        return create(name, feRegion, MockParameterRef.createParams());
    }
    
    public static EventAttrImpl create(String name, int feRegion,
                                            ParameterRef[] parms){
        return new EventAttrImpl(name, MockFERegion.create(feRegion), parms);
    }
    
    public static EventAttrImpl createWallFallAttr(){
        return create("Fall of the Berlin Wall Event", 543);
    }
}
