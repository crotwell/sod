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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.util.StringTokenizer;


public class WaveformCountProcessor implements WaveformMonitor {

    public WaveformCountProcessor(Element config) throws SQLException {
			waveformCounter = getNumWaveforms("waveforms.txt");
    }
    public void update(EventChannelPair ecp) {
        Status processorSuccess = Status.get(Stage.PROCESSOR,Standing.SUCCESS);
        if(ecp.getStatus() == processorSuccess) {
            waveformCounter++;
            FileWriter fileWriter = null;
            try {
                fileWriter = new FileWriter("waveforms.txt");
                fileWriter.write(description + waveformCounter);
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
	private int getNumWaveforms(String fileName) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			StringTokenizer line = new StringTokenizer(reader.readLine());
			StringTokenizer descTokenizer = new StringTokenizer(description);
			int tokenCnt = descTokenizer.countTokens();
			String numWaveforms = null;
			for(int i=0;i<=tokenCnt;i++) {
				numWaveforms = line.nextToken();
			}
			return Integer.parseInt(numWaveforms);
		} catch (IOException e) {
			logger.debug("This exception occurs for runs other than AtmostOnce" +
							" and AtleastOnce when the waveforms.txt is not found ",e);
			return 0;
		}
	}
    int waveformCounter = 0;
	private static final String description = "Number of Waveforms processed = ";
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(WaveformCountProcessor.class);
}


