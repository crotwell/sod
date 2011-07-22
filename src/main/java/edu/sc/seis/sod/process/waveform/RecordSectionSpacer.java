package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
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
        List<DataSetSeismogram> distBin = new ArrayList<DataSetSeismogram>();
        for(DataSetSeismogram cur: remaining) {
            double dist = ((QuantityImpl)dists.get(cur)).getValue();
            if (dist < nextSlot - idealDegreesBetweenSeis + minimumDegreesBetweenSeis) {
                // too close to last added
                continue;
            } else if (dist < nextSlot + idealDegreesBetweenSeis) {
                // possible winner
                distBin.add(cur);
            } else {
                // too far away, accept best in bin and go to next bin
                while (distBin.size() > 0 && dist > nextSlot + idealDegreesBetweenSeis) {
                    DataSetSeismogram best = bestFromBin(distBin, nextSlot, dists);
                    accepted.add(best);
                    double bestDist = dists.get(best).getValue(UnitImpl.DEGREE);
                    System.out.println("Winner: "+bestDist+"  "+best);
                    Iterator<DataSetSeismogram> binIter = distBin.iterator();
                    while (binIter.hasNext()) {
                        DataSetSeismogram dss = binIter.next();
                        if (dists.get(dss).getValue(UnitImpl.DEGREE) < bestDist+minimumDegreesBetweenSeis) {
                            binIter.remove();
                        }
                    }
                    nextSlot = bestDist + idealDegreesBetweenSeis;
                }
                while (dist > nextSlot + idealDegreesBetweenSeis) {
                    nextSlot += idealDegreesBetweenSeis/2;
                }
                distBin.add(cur);
            }
        }
        // get best from last bin
        if (distBin.size() > 0) {
            DataSetSeismogram best = bestFromBin(distBin, nextSlot, dists);
            accepted.add(best);
        }
        return accepted;
    }
    
    
    public double getMinimumDegreesBetweenSeis() {
        return minimumDegreesBetweenSeis;
    }

    
    public double getIdealDegreesBetweenSeis() {
        return idealDegreesBetweenSeis;
    }

    protected DataSetSeismogram bestFromBin(List<DataSetSeismogram> distBin, double nextSlot, Map<DataSetSeismogram, QuantityImpl> dists) {
        DataSetSeismogram best = distBin.get(0);
        double bestScore = calcScore(best, dists.get(best), nextSlot);
        for (DataSetSeismogram dss : distBin) {
            double curScore = calcScore(dss, dists.get(dss), nextSlot);
            if (curScore > bestScore) {
                best = dss;
                bestScore = curScore;
            }
        }
        return best;
    }
    
    protected double calcScore(DataSetSeismogram dss, QuantityImpl dist, double nextSlot) {
        double sToN = 0;
        if(dss.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM) != null) {
            sToN = Double.parseDouble((String)dss.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM));
        }
        double distMetric = 1-Math.abs(nextSlot-dist.getValue(UnitImpl.DEGREE))/idealDegreesBetweenSeis;
        System.out.println("Score: distMetric"+distMetric+"  ston:"+(sToN-1)+"  "+dss);
        return distMetric + distMetric * (sToN-1);
    }
            
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RecordSectionSpacer.class);

    private double minimumDegreesBetweenSeis, idealDegreesBetweenSeis;
}
