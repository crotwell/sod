package edu.sc.seis.sod.subsetter.networkArm;

import edu.sc.seis.sod.*;
import edu.iris.Fissures.IfNetwork.*;
import edu.iris.Fissures.network.*;
import org.w3c.dom.*;

import java.io.*;

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

public class PrintlineChannelProcessor implements NetworkArmProcess {
    /**
     * Creates a new <code>PrintlineChannelProcessor</code> instance.
     *
     * @param config an <code>Element</code> value
     */
    public PrintlineChannelProcessor (Element config){
        filename = SodUtil.getNestedText(config);
    }

    /**
     * Describe <code>process</code> method here.
     *
     * @param network a <code>NetworkAccess</code> value
     * @param channel a <code>Channel</code> value
     * @param cookies a <code>CookieJar</code> value
     */
    public void process(NetworkAccess network, Channel channel, CookieJar cookies) {
        if (filename != null && filename.length() != 0) {
            try {
                FileWriter fwriter = new FileWriter(filename, true);
                BufferedWriter bwriter = new BufferedWriter(fwriter);
                bwriter.write(ChannelIdUtil.toString(channel.get_id()), 0, ChannelIdUtil.toString(channel.get_id()).length());
                bwriter.newLine();
                bwriter.close();
            } catch(Exception e) {
                
            }
        } else {
            System.out.println(ChannelIdUtil.toString(channel.get_id()));
        } // end of else
        
    }
    
    String filename = null;
    
}// PrintlineChannelProcessor
