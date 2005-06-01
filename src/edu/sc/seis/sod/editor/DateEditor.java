/**
 * DateEditor.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;
import javax.xml.transform.TransformerException;
import net.sf.nachocalendar.CalendarFactory;
import net.sf.nachocalendar.components.CalendarUtils;
import net.sf.nachocalendar.components.DateField;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import edu.iris.Fissures.Quantity;
import edu.iris.Fissures.model.ISOTime;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.iris.Fissures.model.QuantityImpl;
import edu.iris.Fissures.model.UnitImpl;
import edu.sc.seis.fissuresUtil.chooser.ClockUtil;
import edu.sc.seis.fissuresUtil.exceptionHandler.GlobalExceptionHandler;
import edu.sc.seis.sod.SodUtil;

public class DateEditor implements EditorPlugin {

    public DateEditor() {
        for(int i = 0; i < REL_TYPES.length; i++) {
            relTypesSet.add(REL_TYPES[i]);
        }
    }

    public JComponent getGUI(final Element element) throws TransformerException {
        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalGlue());
        Box absRelChooser = Box.createHorizontalBox();
        absRelChooser.setBorder(new TitledBorder("Type of date"));
        absRelChooser.add(Box.createHorizontalGlue());
        ButtonGroup aRCGroup = new ButtonGroup();
        JRadioButton absButton = new JRadioButton("Absolute");
        aRCGroup.add(absButton);
        absRelChooser.add(absButton);
        absRelChooser.add(Box.createHorizontalStrut(10));
        JRadioButton relButton = new JRadioButton("Relative");
        aRCGroup.add(relButton);
        absRelChooser.add(relButton);
        absRelChooser.add(Box.createHorizontalGlue());
        vBox.add(absRelChooser);
        vBox.add(Box.createVerticalStrut(10));
        final Box editorBox = Box.createHorizontalBox();
        if(isRelativeDate(SodUtil.getFirstEmbeddedElement(element))) {
            aRCGroup.setSelected(relButton.getModel(), true);
            editorBox.add(getRelativeDateGUI(element));
        } else {
            aRCGroup.setSelected(absButton.getModel(), true);
            editorBox.add(getAbsoluteDateGUI(element));
        }
        vBox.add(editorBox);
        vBox.add(Box.createVerticalGlue());
        ActionListener absRelActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Element firstEl = SodUtil.getFirstEmbeddedElement(element);
                String firstElName = firstEl.getTagName();
                String buttonName = ((JRadioButton)e.getSource()).getText();
                try {
                    JComponent comp = null;
                    boolean replaceComp = false;
                    if(!isRelativeDate(firstEl)
                            && buttonName.equals("Relative")) {
                        comp = getRelativeDateGUI(element);
                        replaceComp = true;
                    } else if(isRelativeDate(firstEl)
                            && buttonName.equals("Absolute")) {
                        comp = getAbsoluteDateGUI(element);
                        replaceComp = true;
                    }
                    if(replaceComp) {
                        replaceChildComponent(editorBox, comp);
                    }
                } catch(TransformerException ex) {
                    GlobalExceptionHandler.handle(ex);
                }
            }
        };
        absButton.addActionListener(absRelActionListener);
        relButton.addActionListener(absRelActionListener);
        return vBox;
    }

    protected void printOptions(Element element) {
        NodeList nodes = element.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if(n instanceof Element) {
                printOptions((Element)n);
            } else if(n instanceof Text) {
                Text textNode = (Text)n;
                if(!textNode.getNodeValue().trim().equals("")) {
                    System.out.println(getFullName((Element)textNode.getParentNode())
                            + "=" + textNode.getNodeValue() + "\n");
                } else {
                    // ignore whitespace
                }
            }
        }
    }

    protected String getFullName(Element e) {
        if(e.getParentNode() != null && e.getParentNode() instanceof Element) {
            return getFullName((Element)e.getParentNode()) + "/"
                    + e.getTagName();
        } else {
            return "/" + e.getTagName();
        }
    }

    public static void replaceChildComponent(JComponent parent, JComponent child) {
        parent.removeAll();
        if(child != null) {
            parent.add(child);
        }
        parent.revalidate();
    }

    private JComponent getAbsoluteDateGUI(Element element)
            throws TransformerException {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        Element firstEl = SodUtil.getFirstEmbeddedElement(element);
        MicroSecondDate date = ClockUtil.now();
        if(firstEl != null) {
            if(relTypesSet.contains(firstEl.getTagName())) {
                cleanOutElement(element);
                insertDateNodesIntoElement(element, date);
            } else {
                cal.setTime(date);
                Node node = XPathAPI.selectSingleNode(element, "year/text()");
                cal.set(Calendar.YEAR, Integer.parseInt(node.getNodeValue()));
                node = XPathAPI.selectSingleNode(element, "month/text()");
                cal.set(Calendar.MONTH,
                        Integer.parseInt(node.getNodeValue()) - 1);
                node = XPathAPI.selectSingleNode(element, "day/text()");
                cal.set(Calendar.DAY_OF_MONTH,
                        Integer.parseInt(node.getNodeValue()));
                date = new MicroSecondDate(cal.getTime());
            }
        } else {
            Node node = XPathAPI.selectSingleNode(element, "text()");
            if(node != null) {
                Text text = (Text)node;
                ISOTime iso = new ISOTime(text.getNodeValue().trim());
                date = iso.getDate();
                element.removeChild(node);
            }
            insertDateNodesIntoElement(element, date);
        }
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        DateFormat dFormat = new SimpleDateFormat("MMM dd, yyyy");
        dFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        DateFormatter dateFormatter = new DateFormatter(dFormat);
        DateField dateField = CalendarFactory.createDateField(dateFormatter);
        if(element.getTagName().equals("startTime")) {
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            date = new MicroSecondDate(cal.getTime());
        } else if(element.getTagName().equals("endTime")) {
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            date = new MicroSecondDate(cal.getTime());
        }
        dateField.setValue(date);
        dateField.addChangeListener(new DateChangeListener(element));
        box.add(dateField);
        box.add(Box.createHorizontalGlue());
        return box;
    }

    private void insertDateNodesIntoElement(Element element,
                                            MicroSecondDate date) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTime(date);
        Document elDoc = element.getOwnerDocument();
        Node parentNode = elDoc.createElement("year");
        element.appendChild(parentNode);
        Node childNode = elDoc.createTextNode("" + cal.get(Calendar.YEAR));
        parentNode.appendChild(childNode);
        parentNode = elDoc.createElement("month");
        element.appendChild(parentNode);
        childNode = elDoc.createTextNode("" + (cal.get(Calendar.MONTH) + 1));
        parentNode.appendChild(childNode);
        parentNode = elDoc.createElement("day");
        element.appendChild(parentNode);
        childNode = elDoc.createTextNode("" + cal.get(Calendar.DAY_OF_MONTH));
        parentNode.appendChild(childNode);
    }

    private void insertRelativeNode(Element parent, String relType, Quantity q) {
        Document elDoc = parent.getOwnerDocument();
        Node relNode = elDoc.createElement(relType);
        parent.appendChild(relNode);
        if(!relType.equals("now")) {
            Node grandChild = elDoc.createElement("value");
            Node greatGrandChild = elDoc.createTextNode("" + (int)q.value);
            relNode.appendChild(grandChild);
            grandChild.appendChild(greatGrandChild);
            grandChild = elDoc.createElement("unit");
            greatGrandChild = elDoc.createTextNode(q.the_units.toString());
            relNode.appendChild(grandChild);
            grandChild.appendChild(greatGrandChild);
        }
    }

    public static void cleanOutElement(Element element) {
        NodeList nl = element.getChildNodes();
        for(int i = nl.getLength() - 1; i >= 0; i--) {
            Node n = nl.item(i);
            element.removeChild(n);
        }
    }

    private boolean isRelativeDate(Element el) {
        if(el != null) {
            return relTypesSet.contains(el.getTagName());
        }
        return false;
    }

    private JComponent getRelativeDateGUI(final Element element)
            throws TransformerException {
        if(!isRelativeDate(SodUtil.getFirstEmbeddedElement(element))) {
            cleanOutElement(element);
            insertRelativeNode(element, "now", null);
        }
        Box vBox = Box.createVerticalBox();
        Box directionBox = Box.createHorizontalBox();
        directionBox.setBorder(new TitledBorder("Time relative to run"));
        directionBox.add(Box.createHorizontalGlue());
        ButtonGroup directionBG = new ButtonGroup();
        final JRadioButton nowButton = new JRadioButton("Now");
        directionBG.add(nowButton);
        directionBox.add(nowButton);
        final JRadioButton earlierButton = new JRadioButton("Earlier");
        directionBG.add(earlierButton);
        directionBox.add(earlierButton);
        final JRadioButton laterButton = new JRadioButton("Later");
        directionBG.add(laterButton);
        directionBox.add(laterButton);
        directionBox.add(Box.createHorizontalGlue());
        vBox.add(directionBox);
        vBox.add(Box.createVerticalStrut(10));
        final Box editorBox = Box.createVerticalBox();
        Element firstEl = SodUtil.getFirstEmbeddedElement(element);
        String firstElName = firstEl.getTagName();
        if(firstElName.equals("earlier")) {
            directionBG.setSelected(earlierButton.getModel(), true);
            editorBox.add(EditorUtil.makeTimeIntervalTwiddler(firstEl));
        } else if(firstElName.equals("later")) {
            directionBG.setSelected(laterButton.getModel(), true);
            editorBox.add(EditorUtil.makeTimeIntervalTwiddler(firstEl));
        } else {
            directionBG.setSelected(nowButton.getModel(), true);
        }
        ActionListener dirButtonActionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Element firstEl = SodUtil.getFirstEmbeddedElement(element);
                String firstElName = firstEl.getTagName();
                try {
                    String buttonName = ((JRadioButton)e.getSource()).getText()
                            .toLowerCase();
                    if(!buttonName.equals(firstElName)) {
                        editorBox.removeAll();
                        cleanOutElement(element);
                        Quantity q = null;
                        boolean isNow = buttonName.equals("now");
                        if(!isNow) {
                            q = new QuantityImpl(2, UnitImpl.WEEK);
                        }
                        insertRelativeNode(element, buttonName, q);
                        firstEl = SodUtil.getFirstEmbeddedElement(element);
                        replaceChildComponent(editorBox, (isNow ? null
                                : EditorUtil.makeTimeIntervalTwiddler(firstEl)));
                    }
                } catch(Exception ex) {
                    GlobalExceptionHandler.handle("problem adding timeinterval twiddler",
                                                  ex);
                }
            }
        };
        nowButton.addActionListener(dirButtonActionListener);
        earlierButton.addActionListener(dirButtonActionListener);
        laterButton.addActionListener(dirButtonActionListener);
        vBox.add(editorBox);
        return vBox;
    }

    private class DateChangeListener implements ChangeListener {

        public DateChangeListener(Element el) {
            element = el;
        }

        Element element;

        public void stateChanged(ChangeEvent e) {
            DateField df = (DateField)e.getSource();
            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            MicroSecondDate mdate = ClockUtil.now();
            try {
                mdate = new MicroSecondDate(CalendarUtils.convertToDate(df.getValue()));
            } catch(ParseException ex) {
                GlobalExceptionHandler.handle("problem parsing date", ex);
            }
            cal.setTime(mdate);
            try {
                Node node = XPathAPI.selectSingleNode(element, "year/text()");
                node.setNodeValue("" + cal.get(Calendar.YEAR));
                node = XPathAPI.selectSingleNode(element, "month/text()");
                node.setNodeValue("" + (cal.get(Calendar.MONTH) + 1));
                node = XPathAPI.selectSingleNode(element, "day/text()");
                node.setNodeValue("" + cal.get(Calendar.DAY_OF_MONTH));
            } catch(TransformerException ex) {
                GlobalExceptionHandler.handle("problem converting date to xml",
                                              ex);
            }
        }
    }

    private Set relTypesSet = new HashSet();

    private static final String[] REL_TYPES = {"earlier", "later", "now"};
}