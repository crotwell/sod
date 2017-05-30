/**
 * WPHTemplate.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;

import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.common.UnitImpl;
import edu.sc.seis.sod.model.status.Stage;
import edu.sc.seis.sod.model.status.Standing;
import edu.sc.seis.sod.model.status.Status;
import edu.sc.seis.sod.status.AllTypeTemplate;
import edu.sc.seis.sod.util.display.ThreadSafeDecimalFormat;
import edu.sc.seis.sod.util.time.ClockUtil;

public class WPHTemplate extends AllTypeTemplate {

    public WPHTemplate() throws SQLException {
        ecs = SodDB.getSingleton();
    }

    public String getResult() {
        int numSuccessful = ecs.getNumSuccessful();
        double elapsedTime = getElapsedTime();
        return df.format(numSuccessful / elapsedTime);
    }

    private double getElapsedTime() {
        MicroSecondDate now = ClockUtil.now();
        MicroSecondDate startTime = Start.getStartTime();
        TimeInterval elapsedTime = now.subtract(startTime);
        return elapsedTime.convertTo(UnitImpl.HOUR).getValue();
    }

    private ThreadSafeDecimalFormat df = new ThreadSafeDecimalFormat("0.00");

    private SodDB ecs;

    private static final Status SUCCESS = Status.get(Stage.PROCESSOR,
                                                     Standing.SUCCESS);
}