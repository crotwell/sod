/**
 * WPHTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status.waveformArm;

import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.AllTypeTemplate;
import java.sql.SQLException;

public class WPHTemplate extends AllTypeTemplate{
    public WPHTemplate() throws SQLException{
        ecs = new JDBCEventChannelStatus();
    }
    public String getResult() {
        try {
            return "" + ecs.getNumOfStatus(Status.get(Stage.PROCESSOR,
                                                      Standing.SUCCESS))/getElapsedTime();
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
            return "";
        }
    }

    private double getElapsedTime() {
        return ClockUtil.now().subtract(Start.getStartTime()).convertTo(UnitImpl.HOUR).getValue();
    }

    private JDBCEventChannelStatus ecs;


}

