/**
 * StationTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfNetwork.Station;

public interface StationTemplate {
    public String getResult(Station station);
}

