/**
 * StationTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.iris.Fissures.IfNetwork.Station;

public interface StationTemplate {
    public String getResult(Station station);
}

