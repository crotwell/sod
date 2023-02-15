/**
 * LongShortStoN.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

import java.time.Duration;
import java.util.LinkedList;

import edu.sc.seis.seisFile.TimeUtils;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;

/** Adapted from reftrg.f from Tom Owens and reftrig.c from Passcal.
 * c
 c     routine to apply the reftek trigger algorithm
 c     to a designated SAC file
 c     LTA is initialized to STA after 2 STA time constants
 c     Trigger detection begins after trgdly seconds
 c
 c     compile with: f77 reftrg.f $SACDIR/lib/sac.a -f68881
 c
 c     Written by T.J. Owens, August 16, 1988
 c
 */
public class LongShortStoN {

    /**
     * @param longTime Time Interval for the long term average
     * @param shortTime Time Interval for the short term average
     * @param threshold ration of short to long termaverages above which a trigger is declared
     **/
    public LongShortStoN(Duration longTime, Duration shortTime, float threshold) {
        this(longTime, shortTime, threshold, shortTime.multipliedBy(2));
    }


    /**
     * @param longTime Time Interval for the long term average
     * @param shortTime Time Interval for the short term average
     * @param threshold ration of short to long termaverages above which a trigger is declared
     **/
    public LongShortStoN(Duration longTime, Duration shortTime, float threshold, Duration delay) {
        this(longTime, shortTime, threshold, delay, Duration.ofSeconds(100));
    }

    public LongShortStoN(Duration longTime, Duration shortTime, float threshold, Duration delay, Duration meanTime) {
        if (longTime.toNanos() <= shortTime.toNanos()) {
            throw new IllegalArgumentException("longTime must be longer than shortTime, longTime="+longTime+
                                                   "  shortTime="+shortTime);
        }
        if (delay.toNanos() < shortTime.toNanos()) {
            throw new IllegalArgumentException("delay must be longer than shortTime, shortTime="+shortTime+
                                                   "  delay="+delay);
        }
        this.longTime = longTime;
        this.shortTime = shortTime;
        this.threshold = threshold;
        this.delay = delay;
        this.meanTime = meanTime;
    }


    public LongShortTrigger[] calcTriggers(LocalSeismogramImpl seis) throws FissuresException {
        LinkedList out = new LinkedList();
        float[] seisData = seis.get_as_floats();

        //   establish number of points in LTA and STA windows
        //    as well as in trgdly

        double dt = TimeUtils.durationToDoubleSeconds(seis.getSampling().getPeriod());
        int nlta=(int)(TimeUtils.durationToDoubleSeconds(longTime) / dt) + 1;
        int nsta=(int)(TimeUtils.durationToDoubleSeconds(shortTime) / dt) + 1;
        int ntdly=(int)(TimeUtils.durationToDoubleSeconds(delay) / dt) + 1;
        int nmean=(int)(TimeUtils.durationToDoubleSeconds(meanTime) / dt) + 1;

        if (Duration.between(seis.getBeginTime(), seis.getEndTime()).toNanos() < delay.toNanos() || nsta > ntdly || ntdly > seis.getNumPoints()) {
            // seis is too short, so no trigger possible
            return new LongShortTrigger[0];
        }
        /*  get weighting factor  */
        float csta = 1.0f / nsta ;
        float clta = 1.0f / nlta ;
        float cmean = 1.0f / nmean ;

        float mean = 0 ;
        float mean1 = mean ;

        /* now start calculations for first two windows sta=lta */
        float sta = 0 ;
        float lta = 0 ;
        float trg = 0 ;   /* previous value of trigger */
        float dat;
        float ratio;
        boolean hold = false;

        for(int i=0 ; i < 2*nsta ; i++) {
            mean = mean + ( seisData[i]-mean)*cmean ;
            dat = seisData[i] - mean ;
            mean1 = mean1 + (dat - mean1)*cmean ;
            dat = dat - mean1 ;

            sta = sta + (Math.abs(dat) - sta)*csta ;
            lta = lta + (Math.abs(dat) - lta)*csta ;
            ratio = sta/lta ;

        }
        /*  now get rest of trace */
        for(int i=2*nsta ; i < seisData.length ; i++) {
            /* up date mean */
            mean = mean + ( seisData[i]-mean)*cmean ;
            dat = seisData[i] - mean ;
            mean1 = mean1 + (dat - mean1)*cmean ;
            dat = Math.abs(dat - mean1);

            sta = sta + (dat - sta)*csta ;
            if( (trg==1) && (hold==true)) {
                /* do not change lta */
            } else {
                lta = lta + (dat - lta)*clta ;
            }
            ratio = sta/lta ;

            if (ratio >= threshold) {
                LongShortTrigger trigger = new LongShortTrigger(seis,
                                                                i,
                                                                ratio,
                                                                sta,
                                                                lta);
                out.add(trigger);
            }
        }
        LongShortTrigger[] trigger = (LongShortTrigger[])out.toArray(new LongShortTrigger[0]);
        return trigger;
    }

    public LongShortTrigger[] calcTriggersTJO(LocalSeismogramImpl seis) throws FissuresException {
        LinkedList out = new LinkedList();
        float[] seisData = seis.get_as_floats();

        //   establish number of points in LTA and STA windows
        //    as well as in trgdly

        double dt = TimeUtils.durationToDoubleSeconds(seis.getSampling().getPeriod());
        int nlta=(int)(TimeUtils.durationToDoubleSeconds(longTime) / dt) + 1;
        int nsta=(int)(TimeUtils.durationToDoubleSeconds(shortTime) / dt) + 1;
        int ntdly=(int)(TimeUtils.durationToDoubleSeconds(delay) / dt) + 1;
        

        if (Duration.between(seis.getBeginTime(), seis.getEndTime()).toNanos() < delay.toNanos() || nsta > ntdly || ntdly > seis.getNumPoints()) {
            // seis is too short, so no trigger possible
            return new LongShortTrigger[0];
        }

        //  n100 is number of data points in 100 second window
        //      (needed for data mean calculation)

        int n100=(int)(100./dt) + 1;

        //     clta and csta are constants in trigger algoritms

        float clta=1.0f/nlta;
        float csta=1.0f/nsta;

        float xmean=0.0f;

        float ylta=0;
        float prevylta=0;
        float ysta=0;
        float prevysta=0;

        // initialize STA, start at delay and sum backwards
        for (int j = 0; j < nsta && j < ntdly; j++) {
            ysta += seisData[ntdly-j-1];
        }
        // initialize LTA, start at delay and sum backwards
        for (int j = 0; j < nlta && j < ntdly; j++) {
            ylta += seisData[ntdly-j-1];
        }
        int nmean = 0;
        for (nmean = 0; nmean < n100 && nmean < ntdly; nmean++) {
            xmean += seisData[ntdly-nmean-1];
        }

        //    start the triggering process
        for (int i = ntdly; i < seisData.length; i++) {
            //    after 100 seconds, data mean is mean of previous 100 seconds only
            if (nmean == n100) {
                xmean -= seisData[i-n100];
            } else {
                nmean++;
            }
            xmean += seisData[i];

            //    LTA value calculated as per REFTEK algorithm
            prevylta = ylta;
            float nextData = Math.abs(seisData[i] - xmean/nmean);
            ylta = clta*nextData
                + (1-clta)*prevylta
                - (i<nlta?0:clta*Math.abs(seisData[i-nlta] - xmean/nmean));
            // don't get index of of bounds

            //    STA value calculated as per REFTEK algorithm
            prevysta = ysta;
            ysta = csta*nextData
                + (1-csta)*prevysta
                - (i<nsta?0:csta*Math.abs(seisData[i-nsta] - xmean/nmean));

            //   rat is STA/LTA at each time point
            float ratio;
            if (ylta != 0) {
                ratio=ysta/ylta;

            } else {
                // in this case, declare a trigger if ysta != 0, otherwise not
                if (ysta != 0) {
                    ratio = threshold;
                } else {
                    ratio = 0;
                }
            }
            if (ratio >= threshold) {
                LongShortTrigger trigger = new LongShortTrigger(seis,
                                                                i,
                                                                ratio,
                                                                ysta,
                                                                ylta);
                out.add(trigger);
            }
        }
        LongShortTrigger[] trigger = (LongShortTrigger[])out.toArray(new LongShortTrigger[0]);
        return trigger;
    }

    protected Duration longTime;
    protected Duration shortTime;
    protected Duration delay;
    protected float threshold;
    protected Duration meanTime;
}





