package edu.sc.seis.sod.subsetter.channel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.ChannelId;
import edu.iris.Fissures.IfNetwork.ChannelNotFound;
import edu.iris.Fissures.IfNetwork.Instrumentation;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.IfNetwork.NetworkFinder;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ResponsePrint;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.status.ChannelFormatter;

public class ResponseWriter implements ChannelSubsetter {

    public ResponseWriter(Element config) throws ConfigurationException {
        String dirName = SodUtil.getNestedText(SodUtil.getElement(config,
                                                                  "directory"));
        directory = new File(dirName);
        directory.mkdirs();
        cf = new ChannelFormatter(SodUtil.getElement(config, "filePattern"),
                                  true);
    }

    public boolean accept(Channel chan) throws Exception {
        PrintWriter printWriter = null;
        try {
            ChannelId channel_id = chan.get_id();
            NetworkAccess network = Start.getNetworkArm()
                    .getNetwork(chan.get_id().network_id);
            Instrumentation inst = network.retrieve_instrumentation(channel_id,
                                                                    channel_id.begin_time);
            String response = ResponsePrint.printResponse(channel_id, inst);
            String location = cf.getResult(chan);
            File f = new File(directory, location);
            f.getParentFile().mkdirs();
            FileOutputStream fileStream = new FileOutputStream(f);
            printWriter = new PrintWriter(fileStream);
            printWriter.print(response);
            printWriter.close();
        } catch(ChannelNotFound ex) {
            GlobalExceptionHandler.handle("Channel not found: "
                    + ChannelIdUtil.toString(chan.get_id()), ex);
            return false;
        } catch(FileNotFoundException fe) {
            GlobalExceptionHandler.handle("Error while response file for "
                    + ChannelIdUtil.toString(chan.get_id()), fe);
            return false;
        }
        return true;
    }

    protected NetworkFinder finder;

    protected ChannelFormatter cf;

    protected File directory;
}
