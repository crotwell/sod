package edu.sc.seis.sod.status.waveformArm;

import java.sql.SQLException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.AllTypeTemplate;

public class NumSuccessfulECPTemplate extends AllTypeTemplate{
    public NumSuccessfulECPTemplate() throws SQLException{
        ecs = new JDBCEventChannelStatus();
    }

    public String getResult(){
        try {
            return "" + ecs.getNumOfStatus(Status.get(Stage.PROCESSOR,
                                                      Standing.SUCCESS));
        } catch (SQLException e) {
            GlobalExceptionHandler.handle(e);
            return "";
        }
    }

    private JDBCEventChannelStatus ecs;
}

