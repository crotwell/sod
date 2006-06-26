package edu.sc.seis.sod.example;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.subsetter.origin.OriginSubsetter;

public class OriginSubsetterExample implements OriginSubsetter {

    public StringTree accept(EventAccessOperations eventAccess,
                             EventAttr eventAttr,
                             Origin preferred_origin) throws Exception {
        return new Pass(this);
    }
}
