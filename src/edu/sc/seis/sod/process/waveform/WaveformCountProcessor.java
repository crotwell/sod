package edu.sc.seis.sod.process.waveform;
import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.database.waveform.JDBCEventChannelStatus;
import edu.sc.seis.sod.status.StringTreeLeaf;
import java.io.FileWriter;
import java.sql.SQLException;
import org.w3c.dom.Element;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import edu.sc.seis.sod.EventChannelPair;
import java.io.IOException;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;


public class WaveformCountProcessor implements WaveformMonitor {
	
    public WaveformCountProcessor(Element config) throws SQLException {
    }
	public void update(EventChannelPair ecp) {
		Status processorSuccess = Status.get(Stage.PROCESSOR,Standing.SUCCESS);
		if(ecp.getStatus() == processorSuccess) {
			waveformCounter++;
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter("waveforms.txt");
				fileWriter.write("Number of Waveforms processed = " + waveformCounter);
			}catch(IOException ie) {
				GlobalExceptionHandler.handle("problem writing the waveformCount into file ",ie);
			}finally {
				try {
					fileWriter.close();
				}catch(IOException ie) {
					GlobalExceptionHandler.handle("problem closing the output file" +
													  "in WaveformCountProcessor ",ie);
				}
			}
		}
	}
	int waveformCounter = 0;
}
