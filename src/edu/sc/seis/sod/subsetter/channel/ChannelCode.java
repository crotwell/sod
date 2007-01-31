package edu.sc.seis.sod.subsetter.channel;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;
import org.w3c.dom.Element;

public class ChannelCode implements ChannelSubsetter {

    public ChannelCode(Element config) throws UserConfigurationException {
        this(SodUtil.getNestedText(config));
    }

    public ChannelCode(String string) throws UserConfigurationException {
        acceptedCode = string.toUpperCase();
        if(acceptedCode.length() == 0
                || (acceptedCode.length() == 1 && acceptedCode.charAt(0) == '*')) {
            required = "***";
        } else if(acceptedCode.length() == 2) {
            if(acceptedCode.charAt(0) == '*') {
                required = "**" + acceptedCode.charAt(1);
            } else if(acceptedCode.charAt(1) == '*') {
                required = acceptedCode.charAt(0) + "**";
            } else {
                throw new UserConfigurationException("A channelCode must be of length 3 if it doesn't contain any '*' characters.  Yours was '"
                        + string + "'");
            }
        } else if(acceptedCode.length() == 3) {
            required = acceptedCode;
        } else {
            throw new UserConfigurationException("A channelCode can be at most of length 3.  Yours was '"
                    + string + "'");
        }
    }

    public StringTree accept(Channel channel, ProxyNetworkAccess network) {
        String code = channel.get_id().channel_code;
        for(int i = 0; i < required.length(); i++) {
            if(required.charAt(i) != '*'
                    && code.charAt(i) != required.charAt(i)) {
                return new StringTreeLeaf(this, false);
            }
        }
        return new StringTreeLeaf(this, true);
    }

    String required;

    private String acceptedCode;
}// GainCode
