/**
 * NetworkTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.NetworkAccess;

public interface NetworkTemplate {
    
    public String getResult(NetworkAccess network);
    
}

