package edu.sc.seis.sod.subsetter.channel;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import org.w3c.dom.Element;
import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.SodUtil;

/**
 * @author <a href="mailto:crotwell@pooh">Philip Crotwell </a>
 */
public class PrintlineChannelProcessor implements ChannelSubsetter {

    public PrintlineChannelProcessor(Element config) {
        filename = SodUtil.getNestedText(config);
    }

    public boolean accept(Channel channel) throws IOException {
        if(filename != null && filename.length() != 0) {
            FileWriter fwriter = new FileWriter(filename, true);
            BufferedWriter bwriter = new BufferedWriter(fwriter);
            bwriter.write(ChannelIdUtil.toString(channel.get_id()),
                          0,
                          ChannelIdUtil.toString(channel.get_id()).length());
            bwriter.newLine();
            bwriter.close();
        } else {
            System.out.println("Channel: "
                    + ChannelIdUtil.toString(channel.get_id()));
        } // end of else
        return true;
    }

    String filename = null;
}// PrintlineChannelProcessor
