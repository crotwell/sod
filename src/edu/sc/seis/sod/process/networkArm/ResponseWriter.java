package edu.sc.seis.sod.process.networkArm;

import edu.iris.Fissures.IfNetwork.*;

import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ResponsePrint;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.networkArm.NetworkArmProcess;
import edu.sc.seis.sod.status.ChannelFormatter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import org.w3c.dom.Element;

public class ResponseWriter implements NetworkArmProcess {

    public ResponseWriter (Element config) throws ConfigurationException  {
        String dirName = SodUtil.getNestedText(SodUtil.getElement(config, "directory"));
        directory = new File(dirName);
        directory.mkdirs();
        cf = new ChannelFormatter(SodUtil.getElement(config, "filePattern"), true);
    }

    public ResponseWriter (NetworkDC netdc){ this(netdc.a_finder()); }

    public ResponseWriter(NetworkFinder netFinder){
        finder = netFinder;
    }
    public void process(NetworkAccess network, Channel chan) throws Exception {
        PrintWriter printWriter = null;
        try {
            ChannelId channel_id = chan.get_id();
            Instrumentation inst = network.retrieve_instrumentation(channel_id,channel_id.begin_time);
            String response = ResponsePrint.printResponse(channel_id,inst);
            String location = cf.getResult(chan);
            File f = new File(directory, location);
            f.getParentFile().mkdirs();
            FileOutputStream fileStream = new FileOutputStream(f);
            printWriter = new PrintWriter(fileStream);
            printWriter.print(response);
            printWriter.close();
        }catch(ChannelNotFound ex) {
            GlobalExceptionHandler.handle("Channel not found: " + ChannelIdUtil.toString(chan.get_id()),ex);
        }catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while response file for " + ChannelIdUtil.toString(chan.get_id()),fe);
        }
    }

    protected NetworkFinder finder;

    protected ChannelFormatter cf;

    protected File directory;
}
