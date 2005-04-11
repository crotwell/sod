package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import edu.iris.Fissures.model.QuantityImpl;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;

/**
 * @author groves Created on Apr 8, 2005
 */
public class RecordSectionSpacer {

    public RecordSectionSpacer() {
        this(new DistanceRange(0, 180));
    }

    public RecordSectionSpacer(DistanceRange range) {
        this(range, 15, 21);
    }

    public RecordSectionSpacer(DistanceRange range, int idealSeismograms,
            int maximumSeismogram) {
        double r = range.getMaxDistance() - range.getMinDistance();
        minimumDegreesBetweenSeis = r / (maximumSeismogram + 1);
        idealDegreesBetweenSeis = r / (idealSeismograms + 1);
    }

    public DataSetSeismogram[] spaceOut(DataSetSeismogram[] dataSeis) {
        Map distMap = new HashMap();
        sortByDistance(dataSeis, distMap);
        List remaining = new ArrayList();
        for(int i = 0; i < dataSeis.length; i++) {
            remaining.add(dataSeis[i]);
        }
        List accepted = new ArrayList();
        double nextSlot = 0;
        while(remaining.size() > 0) {
            double leastPossibleDist = nextSlot
                    - (idealDegreesBetweenSeis - minimumDegreesBetweenSeis);
            Iterator it = remaining.iterator();
            DataSetSeismogram closest = null;
            double closestVal = Double.MAX_VALUE;
            while(it.hasNext()) {
                DataSetSeismogram cur = (DataSetSeismogram)it.next();
                QuantityImpl dist = retrieveOrCalc(cur, distMap);
                if(dist.getValue() >= nextSlot) {
                    if(closest != null
                            && nextSlot - closestVal <= dist.getValue()
                                    - nextSlot) {
                        accepted.add(closest);
                        nextSlot = closestVal + idealDegreesBetweenSeis;
                    } else {
                        it.remove();
                        accepted.add(cur);
                        nextSlot = dist.getValue() + idealDegreesBetweenSeis;
                    }
                    break;
                } else if(dist.getValue() > leastPossibleDist) {
                    closest = cur;
                    closestVal = dist.getValue();
                }
                it.remove();
            }
            if(remaining.size() == 0 && closest != null) {
                accepted.add(closest);
            }
        }
        return (DataSetSeismogram[])accepted.toArray(new DataSetSeismogram[accepted.size()]);
    }

    public static void sortByDistance(DataSetSeismogram[] seismograms,
                                      final Map dists) {
        Arrays.sort(seismograms, new Comparator() {

            public int compare(Object o1, Object o2) {
                QuantityImpl dist1 = retrieveOrCalc((DataSetSeismogram)o1,
                                                    dists);
                QuantityImpl dist2 = retrieveOrCalc((DataSetSeismogram)o2,
                                                    dists);
                if(dist1.lessThan(dist2)) {
                    return -1;
                } else if(dist1.greaterThan(dist2)) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
    }

    public static QuantityImpl retrieveOrCalc(DataSetSeismogram seis,
                                              final Map dists) {
        QuantityImpl dist = (QuantityImpl)dists.get(seis);
        if(dist == null) {
            dist = DisplayUtils.calculateDistance(seis);
            dists.put(seis, dist);
        }
        return dist;
    }

    private double minimumDegreesBetweenSeis, idealDegreesBetweenSeis;
}
