/**
 * NetCodeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class NetCodeEditor implements EditorPlugin {

    public JComponent getGUI(Element element) throws TransformerException {
        if (element.getTagName().equals("networkCode")) {
            return getNetworkCodeGUI(element);
        } else if (element.getTagName().equals("stationCode")) {
            return getStationCodeGUI(element);
        } else if (element.getTagName().equals("siteCode")) {
            return getSiteCodeGUI(element);
        } else if (element.getTagName().equals("channelCode")) {
            return getChannelCodeGUI(element);
        } else if (element.getTagName().equals("orientationCode")) {
            return getOrientationCodeGUI(element);
        } else if (element.getTagName().equals("gainCode")) {
            return getGainCodeGUI(element);
        } else if (element.getTagName().equals("bandCode")) {
            return getBandCodeGUI(element);
        } else {
            throw new IllegalArgumentException("Unknown tag name "+element.getTagName());
        }
    }

    public JComponent getNetworkCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, allNets);
    }


    public JComponent getStationCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, new String[0]);
    }

    public JComponent getSiteCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, allSites);
    }

    public JComponent getChannelCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, new String[0]);
    }

    public JComponent getBandCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, bands);
    }

    public JComponent getGainCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, gains);
    }

    public JComponent getOrientationCodeGUI(Element element) throws TransformerException {
        return initComboBox(element, orientations);
    }

    public JComboBox initComboBox(Element element, String[] vals) throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "text()");
        Text text = (Text)node;
        JComboBox combo = new JComboBox(vals);
        combo.addItem(text.getNodeValue());
        combo.setSelectedItem(text.getNodeValue());
        return combo;
    }

    protected String[] allNets = { "II", "IU", "US", "SP" };

    protected String[] allSites = { "  ", "00", "01", "02" };

    protected String[] orientations = { "E", "N", "Z", "1", "2", "3", "U", "V", "W" };

    protected String[] bands = { "B", "L", "S", "M" };

    protected String[] gains = { "H", "L" };

}

