/**
 * NetCodeEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.JComponent;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
        return EditorUtil.getLabeledComboBox(element, allNets);
    }


    public JComponent getStationCodeGUI(Element element) throws TransformerException {
        return EditorUtil.getLabeledComboBox(element, new String[0]);
    }

    public JComponent getSiteCodeGUI(Element element) throws TransformerException {
        //for some reason, "  " site codes are considered to be null text nodes
        Node node = XPathAPI.selectSingleNode(element, "text()");
        if(node == null) {
            return EditorUtil.getLabeledComboBox(element, allSites, allSites[0]);
        }
        return EditorUtil.getLabeledComboBox(element, allSites);
    }

    public JComponent getChannelCodeGUI(Element element) throws TransformerException {
        return EditorUtil.getLabeledComboBox(element, new String[0]);
    }

    public JComponent getBandCodeGUI(Element element) throws TransformerException {
        return EditorUtil.getLabeledComboBox(element, bands);
    }

    public JComponent getGainCodeGUI(Element element) throws TransformerException {
        return EditorUtil.getLabeledComboBox(element, gains);
    }

    public JComponent getOrientationCodeGUI(Element element) throws TransformerException {
        return EditorUtil.getLabeledComboBox(element, orientations);
    }

    protected String[] allNets = { "II", "IU", "US", "SP" };

    protected String[] allSites = { "  ", "00", "01", "02" };

    protected String[] orientations = { "E", "N", "Z", "1", "2", "3", "U", "V", "W" };

    protected String[] bands = { "B", "L", "S", "M" };

    protected String[] gains = { "H", "L" };

}

