package edu.sc.seis.sod.process.waveform;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import org.w3c.dom.Element;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.EventNetworkPair;
import edu.sc.seis.sod.EventStationPair;
import edu.sc.seis.sod.Stage;
import edu.sc.seis.sod.Standing;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.waveformArm.WaveformMonitor;
import java.io.BufferedWriter;

public class WaveformCountProcessor implements WaveformMonitor {
	
    private static final String WAVEFORM_FILE = "waveforms.txt";
	
    public WaveformCountProcessor(Element config) throws IOException {
		waveformCounter = getNumWaveforms(WAVEFORM_FILE);
    }

    public void update(EventNetworkPair ecp) {
    }

    public void update(EventStationPair ecp) {
    }
    
    public void update(EventChannelPair ecp) {
		Status processorSuccess = Status.get(Stage.PROCESSOR, Standing.SUCCESS);
		if(ecp.getStatus() == processorSuccess) {
			waveformCounter++;
			try {
				File actualOutputLocation = new File(WAVEFORM_FILE).getCanonicalFile();
				actualOutputLocation.getParentFile().mkdirs();
				File temp = File.createTempFile(actualOutputLocation.getName(), null,
												actualOutputLocation.getParentFile());
				BufferedWriter fileWriter = null;
				try {
					fileWriter = new BufferedWriter(new FileWriter(temp));
					fileWriter.write(description + waveformCounter);
				} finally {
					fileWriter.close();
				}
				actualOutputLocation.delete();
				temp.renameTo(actualOutputLocation);
			} catch(IOException ie) {
				GlobalExceptionHandler.handle("problem writing the waveformCount into file ",
											  ie);
			}
		}
    }
	
    private int getNumWaveforms(String fileName) throws IOException {
		File f = new File(fileName);
		if(f.exists()) {
			BufferedReader reader = new BufferedReader(new FileReader(f));
			StringTokenizer line = new StringTokenizer(reader.readLine());
			StringTokenizer descTokenizer = new StringTokenizer(description);
			int tokenCnt = descTokenizer.countTokens();
			String numWaveforms = null;
			for(int i = 0; i <= tokenCnt; i++) {
				numWaveforms = line.nextToken();
			}
			return Integer.parseInt(numWaveforms);
		}
		return 0;
    }
	
    int waveformCounter = 0;
	
    private static final String description = "Number of Waveforms processed = ";
}
