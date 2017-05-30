/**
 * StationTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.sod.model.station.StationImpl;

public interface StationTemplate {
    public String getResult(StationImpl station);
}

