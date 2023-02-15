
package edu.sc.seis.sod.model.common;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;

/** represents the orientation of a single component of a seismometer.
 **/

public class Orientation {
    
    
    Orientation()
    {
    }
    
    public static Orientation of(Channel chan) {
        return new Orientation(chan.getAzimuth().getValue(), chan.getDip().getValue());
    }

    public
    Orientation(float azimuth,
                float dip)
    {
        this.azimuth = azimuth;
        this.dip = dip;
    }

    
    public float getAzimuth() {
        return azimuth;
    }

    
    public float getDip() {
        return dip;
    }

    public float azimuth;
    public float dip;
}
