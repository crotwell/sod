package edu.sc.seis.sod.subsetter.eventArm;

import edu.sc.seis.sod.*;
import java.util.*;
import org.w3c.dom.*;
import edu.iris.Fissures.IfEvent.*;
import edu.iris.Fissures.event.*;
import edu.iris.Fissures.*;

/**
 * NullOriginSubsetter.java
 *
 *
 * Created: Thu Mar 14 14:02:33 2002
 *
 * @author <a href="mailto:">Philip Crotwell</a>
 * @version
 */

public class NullOriginSubsetter implements OriginSubsetter {
    public NullOriginSubsetter () {}

    public NullOriginSubsetter (Element config) { }

    public boolean accept(EventAccessOperations event, Origin e) throws Exception{
        return true;
    }
}// NullOriginSubsetter
