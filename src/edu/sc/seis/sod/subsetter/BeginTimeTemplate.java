/**
 * BeginTimeTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;

import edu.iris.Fissures.IfNetwork.Channel;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.sod.SodUtil;
import java.text.SimpleDateFormat;
import org.w3c.dom.Element;

public class BeginTimeTemplate implements ChannelTemplate {
    public BeginTimeTemplate(Element config){
        sdf = new SimpleDateFormat(SodUtil.getNestedText(config));
    }
    
    public String getResult(Channel chan) {
        return sdf.format(new MicroSecondDate(chan.get_id().begin_time));
    }
    
    SimpleDateFormat sdf;
}

