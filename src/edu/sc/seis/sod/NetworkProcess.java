package edu.sc.seis.sod;

import edu.iris.Fissures.IfNetwork.*;

/**
 * NetworkProcess.java
 *
 *
 * Created: Tue Mar 19 14:10:08 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public interface NetworkProcess extends Process {

    public void process(NetworkAccess network);
    
}// NetworkProcess
