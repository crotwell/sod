/**
 * TextAreaStatusDisplay.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.Status;
import edu.sc.seis.sod.status.EventFormatter;
import edu.sc.seis.sod.status.eventArm.EventArmMonitor;
import edu.sc.seis.sod.status.waveformArm.WaveformArmMonitor;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JTextArea;

public class TextAreaStatusDisplay implements EventArmMonitor, WaveformArmMonitor {
    TextAreaStatusDisplay() {
        this.area  = new JTextArea();
        area.setLineWrap(true);
        area.setRows(25);
    }

    public JTextArea getTextArea() {
        return area;
    }

    JTextArea area;

    LinkedList messages = new LinkedList();

    public void change(EventAccessOperations event, Status status){
        write(EventFormatter.getDefaultResult(event)+" "+status+"\n");
    }

    public void setArmStatus(String status) throws Exception {
        write(status+"\n");
    }

    public void update(EventChannelPair ecp) {
        write(EventFormatter.getDefaultResult(ecp.getEvent())+" "+ChannelIdUtil.toStringNoDates(ecp.getChannel().get_id())+" "+ecp.getStatus()+"\n");
    }

    synchronized void write(String message) {
        area.setText("");
        messages.addLast(message);
        if (messages.size() > MAX_SIZE) {
            messages.remove(0);
        }
        Iterator it = messages.iterator();
        while( it.hasNext()) {
            area.append((String)it.next());
        }
    }

    int MAX_SIZE = 30;

}

