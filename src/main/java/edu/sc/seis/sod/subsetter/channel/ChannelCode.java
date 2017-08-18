package edu.sc.seis.sod.subsetter.channel;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class ChannelCode implements ChannelSubsetter {

    public ChannelCode(Element config) throws UserConfigurationException {
        this(SodUtil.getNestedText(config));
    }

    public ChannelCode(String string) throws UserConfigurationException {
        this.code = string;
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
        required = required.replace('?', '*');
    }

    public StringTree accept(Channel channel, NetworkSource network) {
        String code = channel.getChannelCode();
        for(int i = 0; i < required.length(); i++) {
            if(required.charAt(i) != '*'
                    && code.charAt(i) != required.charAt(i)) {
                return new StringTreeLeaf(this, false);
            }
        }
        return new StringTreeLeaf(this, true);
    }

    String required;

    private String code;
    
    private String acceptedCode;

    public String getCode() {
        return code;
    }
    
}
