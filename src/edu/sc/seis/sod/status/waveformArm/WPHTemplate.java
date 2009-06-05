/**
 * WPHTemplate.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;
import java.text.DecimalFormat;

import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.TimeInterval;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.hibernate.SodDB;
import edu.sc.seis.sod.status.AllTypeTemplate;

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

    private DecimalFormat df = new DecimalFormat("0.00");

    private SodDB ecs;

    private static final Status SUCCESS = Status.get(Stage.PROCESSOR,
                                                     Standing.SUCCESS);
}