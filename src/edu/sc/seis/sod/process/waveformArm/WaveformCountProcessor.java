
package edu.sc.seis.sod.process.waveformArm;
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


public class WaveformCountProcessor implements WaveformProcess {
    public WaveformCountProcessor(Element config) throws SQLException {
        jdbcEventChannelStatus= new JDBCEventChannelStatus();
    }

    public LocalSeismogramResult process(EventAccessOperations event, Channel channel,
                                         RequestFilter[] original, RequestFilter[] available,
                                         LocalSeismogramImpl[] seismograms,
                                         CookieJar cookieJar) throws Exception {

        int num_waveforms = jdbcEventChannelStatus.getNumOfStatus(Status.get(Stage.PROCESSOR,
                                                                             Standing.SUCCESS));
        FileWriter fileWriter = new FileWriter("waveforms.txt");
        fileWriter.write("\n Number of Waveforms processed = " + num_waveforms + "\n");
        fileWriter.close();
        return new LocalSeismogramResult(true, seismograms, new StringTreeLeaf(this, true));
    }
    JDBCEventChannelStatus jdbcEventChannelStatus;
}
