package edu.sc.seis.sod.velocity.seismogram;

import java.util.ArrayList;
import java.util.List;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;

/**
 * @author groves Created on May 25, 2005
 */
public class VelocitySeismogram extends LocalSeismogramImpl {

    public VelocitySeismogram(LocalSeismogramImpl localSeis) {
        super(localSeis, localSeis.getData());
    }

    public static List wrap(LocalSeismogramImpl[] seis) {
        List results = new ArrayList(seis.length);
        for(int i = 0; i < seis.length; i++) {
            results.add(new VelocitySeismogram(seis[i]));
        }
        return results;
    }
}
