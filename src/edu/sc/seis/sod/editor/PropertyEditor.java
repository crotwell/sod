/**
 * PropertyEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import edu.sc.seis.sod.SodUtil;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import java.awt.event.ActionEvent;

public class PropertyEditor implements EditorPlugin {
    public JComponent getGUI(Element element) throws TransformerException {
        JPanel panel = new JPanel(new GridBagLayout());
        int gridy = 0;
        NodeList nl = element.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if(nl.item(i) instanceof Element){
                Element childEl = (Element)nl.item(i);
                addLabel(EditorUtil.getLabel(childEl.getTagName()), panel, gridy);
                addTwiddler(makeTwiddler(childEl), panel, gridy++);
                addSpacer(panel, gridy++, false);
            }
        }
        addSpacer(panel, gridy++, true);
        return panel;
    }

    private void addLabel(JComponent label, JComponent recipient, int gridy){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.EAST;
        recipient.add(label, gbc);
    }

    private void addTwiddler(JComponent twiddler, JComponent recipient, int gridy){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1;
        Dimension minSize = twiddler.getMinimumSize();
        if(minSize.width < 300){ gbc.ipadx = (300 - minSize.width)/2; }
        recipient.add(twiddler, gbc);
    }

    private void addSpacer(JComponent recipient, int gridy, boolean finalSpacer){
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1;
        gbc.gridy = gridy;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        if(finalSpacer){ gbc.weighty = 1; }
        recipient.add(new JPanel(), gbc);
    }

    private JComponent makeTwiddler(Element el) throws TransformerException{
        for (int i = 0; i < timeQuantityEls.length; i++) {
            if(timeQuantityEls[i].equals(el.getTagName())){
                return EditorUtil.makeTimeIntervalTwiddler(el);
            }
        }
        final Text t = (Text)XPathAPI.selectSingleNode(el, "text()");
        for (int i = 0; i < checkBoxEls.length; i++) {
            if(checkBoxEls[i].equals(el.getTagName())){
                final JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(SodUtil.isTrueText(t.getNodeValue()));
                checkBox.addActionListener(new ActionListener(){
                            public void actionPerformed(ActionEvent e) {
                                if(checkBox.isSelected()){ t.setData("TRUE"); }
                                else{ t.setData("FALSE"); }
                            }
                        });
                return checkBox;
            }
        }
        if(el.getTagName().equals("waveformWorkerThreads")){
            return EditorUtil.createNumberSpinner(t, 1, 5, 1);
        }else{
            return EditorUtil.getTextField(t);
        }
    }

    private String[] timeQuantityEls = { "eventLag", "eventQueryIncrement",
            "eventRefreshInterval", "maxRetryDelay" };

    private String[] checkBoxEls = { "removeDatabase", "reopenEvents" };
}


