package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
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

    public RecordSectionSpacer(DistanceRange range,
                               int idealSeismograms,
                               int maximumSeismogram) {
        double r = range.getMaxDistance() - range.getMinDistance();
        minimumDegreesBetweenSeis = r / (maximumSeismogram + 1);
        idealDegreesBetweenSeis = r / (idealSeismograms + 1);
    }

    public DataSetSeismogram[] spaceOut(DataSetSeismogram[] dataSeis) {
        Map distMap = new HashMap();
        for(int i = 0; i < dataSeis.length; i++) {
            QuantityImpl dist = DisplayUtils.calculateDistance(dataSeis[i]);
            if(dist != null) {
                distMap.put(dataSeis[i], dist);
            }else{
                logger.debug("Unable to calculate distance for " + dataSeis[i]);
            }
        }
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
            ListIterator it = remaining.listIterator();
            DataSetSeismogram closest = null;
            double closestVal = Double.MAX_VALUE;
            double closestSToN = Double.MAX_VALUE;
            while(it.hasNext()) {
                DataSetSeismogram cur = (DataSetSeismogram)it.next();
                double dist = ((QuantityImpl)distMap.get(cur)).getValue();
                double sToN = Double.MAX_VALUE;
                if(cur.getAuxillaryData(SaveSeismogramToFile.SVN_PARAM) != null) {
                    sToN = Double.parseDouble((String)cur.getAuxillaryData(SaveSeismogramToFile.SVN_PARAM));
                }
                if(dist >= nextSlot
                        && (closest == null || dist <= nextSlot
                                + minimumDegreesBetweenSeis)) {
                    if(closest != null
                            && nextSlot - closestVal <= dist - nextSlot
                            && closestSToN >= sToN) {
                        accepted.add(closest);
                        nextSlot = closestVal + idealDegreesBetweenSeis;
                    } else {
                        if(closest != null
                                && nextSlot - closestVal > dist - nextSlot) {
                            logger.debug("Rec sec seis chosen for StoN");
                        }
                        it.remove();
                        accepted.add(cur);
                        nextSlot = dist + idealDegreesBetweenSeis;
                    }
                    break;
                } else if(dist > leastPossibleDist) {
                    if(sToN >= closestSToN) {
                        closest = cur;
                        closestVal = dist;
                    } else {
                        logger.debug("Rec sec seis rejected for StoN");
                    }
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
                QuantityImpl dist1 = (QuantityImpl)dists.get(o1);
                QuantityImpl dist2 = (QuantityImpl)dists.get(o2);
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

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RecordSectionSpacer.class);

    private double minimumDegreesBetweenSeis, idealDegreesBetweenSeis;
}
