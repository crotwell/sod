/**
 * DateEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import java.util.Calendar;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import de.wannawork.jcalendar.JCalendarComboBox;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;

public class DateEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        Box box = Box.createHorizontalBox();
        Node node = XPathAPI.selectSingleNode(element, "text()");
        Text text = (Text)node;
        ISOTime iso = new ISOTime(text.getNodeValue().trim());
        Calendar cal = iso.getCalendar();
        JCalendarComboBox calBox = new JCalendarComboBox(cal);
        calBox.addChangeListener(new DateChangeListener(text));
        box.add(calBox);

        return box;
    }

    class DateChangeListener implements ChangeListener {
        public DateChangeListener(Text text) {
            this.text = text;
        }
        Text text;

        public void stateChanged(ChangeEvent e) {
            JCalendarComboBox cal = (JCalendarComboBox)e.getSource();
            MicroSecondDate mdate = new MicroSecondDate(cal.getCalendar().getTime());
            text.setNodeValue(mdate.getFissuresTime().date_time);
        }
    }
}

