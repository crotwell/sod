package edu.sc.seis.sod.process.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.IfNetwork.NetworkAccess;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.SodUtil;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;

/**
 * PrintlineChannelProcessor.java
 * <pre>
 * &lt;printLineChannelProcessor/&gt;
 * </pre>
 *
 * Created: Tue Mar 19 14:08:39 2002
 *
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell</a>
 * @version
 */

public class PrintlineChannelProcessor implements NetworkProcess {
    public PrintlineChannelProcessor (Element config){
        filename = SodUtil.getNestedText(config);
    }

    public void process(NetworkAccess network, Channel channel) throws IOException {
        if (filename != null && filename.length() != 0) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(ChannelIdUtil.toString(channel.get_id()), 0, ChannelIdUtil.toString(channel.get_id()).length());
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println("Channel: "+ChannelIdUtil.toString(channel.get_id()));
        } // end of else

    }

    String filename = null;
}// PrintlineChannelProcessor
