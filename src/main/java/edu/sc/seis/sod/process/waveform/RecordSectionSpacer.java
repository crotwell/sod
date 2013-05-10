package edu.sc.seis.sod.process.waveform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.xml.DataSetSeismogram;
import edu.sc.seis.sod.hibernate.RecordSectionItem;

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
        minimumDegreesBetweenSeis = r / (maximumSeismogram - 1);
        idealDegreesBetweenSeis = r / (idealSeismograms - 1);
    }

    public List<RecordSectionItem> spaceOut(List<RecordSectionItem> recordSectionList) {
        List<RecordSectionItem> remaining = new ArrayList<RecordSectionItem>();
        remaining.addAll(recordSectionList);
        Collections.sort(remaining, new Comparator<RecordSectionItem>() {

            public int compare(RecordSectionItem o1, RecordSectionItem o2) {
                if(o1.getDegrees() < o2.getDegrees()) {
                    return -1;
                } else if(o1.getDegrees() > o2.getDegrees()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        List<RecordSectionItem> accepted = new ArrayList<RecordSectionItem>();
        double nextSlot = 0;
        List<RecordSectionItem> distBin = new ArrayList<RecordSectionItem>();
        for(RecordSectionItem cur: remaining) {
            float dist = cur.getDegrees();
            if (dist < nextSlot - idealDegreesBetweenSeis + minimumDegreesBetweenSeis) {
                // too close to last added
                continue;
            } else if (dist < nextSlot + idealDegreesBetweenSeis) {
                // possible winner
                distBin.add(cur);
            } else {
                // too far away, accept best in bin and go to next bin
                while (distBin.size() > 0 && dist > nextSlot + idealDegreesBetweenSeis) {
                    RecordSectionItem best = bestFromBin(distBin, nextSlot);
                    accepted.add(best);
                    Iterator<RecordSectionItem> binIter = distBin.iterator();
                    while (binIter.hasNext()) {
                        RecordSectionItem dss = binIter.next();
                        if (dss.getDegrees() < best.getDegrees()+minimumDegreesBetweenSeis) {
                            binIter.remove();
                        }
                    }
                    nextSlot = best.getDegrees() + idealDegreesBetweenSeis;
                }
                while (dist > nextSlot + idealDegreesBetweenSeis) {
                    nextSlot += idealDegreesBetweenSeis/2;
                }
                distBin.add(cur);
            }
        }
        // get best from last bin
        if (distBin.size() > 0) {
            RecordSectionItem best = bestFromBin(distBin, nextSlot);
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

    protected RecordSectionItem bestFromBin(List<RecordSectionItem> distBin, double nextSlot) {
        RecordSectionItem best = distBin.get(0);
        double bestScore = calcScore(best, nextSlot);
        for (RecordSectionItem rsi : distBin) {
            double curScore = calcScore(rsi, nextSlot);
            if (curScore > bestScore) {
                best = rsi;
                bestScore = curScore;
            }
        }
        return best;
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

    protected double calcScore(RecordSectionItem rsi, double nextSlot) {
        return calcScore(rsi.getDegrees(), rsi.getsToN(), nextSlot);
    }
    
    protected double calcScore(DataSetSeismogram dss, QuantityImpl dist, double nextSlot) {
        double sToN = 0;
        if(dss.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM) != null) {
            sToN = Double.parseDouble((String)dss.getAuxillaryData(AbstractSeismogramWriter.SVN_PARAM));
        }
        double percentCoverage = 1;
        if(dss.getAuxillaryData(PERCENT_COVERAGE) != null) {
            percentCoverage = Double.parseDouble((String)dss.getAuxillaryData(PERCENT_COVERAGE));
        } else {
            
        }
        return calcScore(dist.getValue(UnitImpl.DEGREE), sToN, nextSlot);
    }
    
    protected double calcScore(double dist, double sToN, double nextSlot) {
        double distMetric = 1-Math.abs(nextSlot-dist)/idealDegreesBetweenSeis;
        return distMetric + distMetric * (sToN-1);
    }
            
    protected String PERCENT_COVERAGE = "PercentCoverage";
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(RecordSectionSpacer.class);

    private double minimumDegreesBetweenSeis, idealDegreesBetweenSeis;
}
