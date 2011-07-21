package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Collections;
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
        this(new RSDistanceRange(0, 180));
    }

    public RecordSectionSpacer(RSDistanceRange range) {
        this(range, 15, 21);
    }

    public RecordSectionSpacer(RSDistanceRange range,
                               int idealSeismograms,
                               int maximumSeismogram) {
        double r = range.getMaxDistance() - range.getMinDistance();
        minimumDegreesBetweenSeis = r / (maximumSeismogram + 1);
        idealDegreesBetweenSeis = r / (idealSeismograms + 1);
    }

    public List<DataSetSeismogram> spaceOut(List<? extends DataSetSeismogram> dataSeis) {
        final Map<DataSetSeismogram, QuantityImpl> dists = new HashMap<DataSetSeismogram, QuantityImpl>();
        List<DataSetSeismogram> remaining = new ArrayList<DataSetSeismogram>();
        for (DataSetSeismogram curr: dataSeis) {
            QuantityImpl dist = DisplayUtils.calculateDistance(curr);
            if(dist != null) {
                remaining.add(curr);
                dists.put(curr, dist);
            } else {
                logger.debug("Unable to calculate distance for " + curr);
            }
        }
        Collections.sort(remaining, new Comparator<DataSetSeismogram>() {

            public int compare(DataSetSeismogram o1, DataSetSeismogram o2) {
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
        List<DataSetSeismogram> accepted = new ArrayList<DataSetSeismogram>();
        double nextSlot = 0;
        while(remaining.size() > 0) {
            double leastPossibleDist = nextSlot
                    - (idealDegreesBetweenSeis - minimumDegreesBetweenSeis);
            ListIterator<DataSetSeismogram> it = remaining.listIterator();
            DataSetSeismogram closest = null;
            double closestVal = Double.MAX_VALUE;
            double closestSToN = Double.MAX_VALUE;
            while(it.hasNext()) {
                DataSetSeismogram cur = (DataSetSeismogram)it.next();
                double dist = ((QuantityImpl)dists.get(cur)).getValue();
                double sToN = Double.MAX_VALUE;
                if(cur.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM) != null) {
                    sToN = Double.parseDouble((String)cur.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM));
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
        return accepted;
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RecordSectionSpacer.class);

    private double minimumDegreesBetweenSeis, idealDegreesBetweenSeis;
}
