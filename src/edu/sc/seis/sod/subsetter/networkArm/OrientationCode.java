package edu.sc.seis.sod.subsetter.networkArm;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.sod.SodUtil;
import org.w3c.dom.Element;
/**
 * sample xml file
 * <pre>
 * &lt;orientationCode&gt;&lt;value&gt;Z &lt;/value&gt;&lt;/orientationCode&gt;
 * </pre>
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version 1.0
 */
public class OrientationCode implements ChannelSubsetter {
    public OrientationCode(Element config) {
        importantPiece = SodUtil.getNestedText(config).charAt(0);
    }

    public boolean accept(Channel channel) throws Exception {
        if(channel.get_id().channel_code.charAt(2) == importantPiece) {
            return true;
        }else{ return false; }
    }

    private char importantPiece;

}//OrientationCode
