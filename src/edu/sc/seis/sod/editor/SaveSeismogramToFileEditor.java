/**
 * SaveSeismogramToFileEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import java.awt.event.ActionEvent;

public class SaveSeismogramToFileEditor implements EditorPlugin{

    public SaveSeismogramToFileEditor(){

    }

    public JComponent getGUI(Element element) throws Exception {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(new TitledBorder(SimpleGUIEditor.getDisplayName(element.getNodeName())));

        Box b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("File Type"));
        b.add(Box.createHorizontalStrut(10));
        final Text text = (Text)XPathAPI.selectSingleNode(element, "fileType/text()");
        final JComboBox fileTypeBox = new JComboBox(fileTypes);
        fileTypeBox.setSelectedItem(text.getNodeValue());
        fileTypeBox.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e) {
                        text.setNodeValue(fileTypeBox.getSelectedItem().toString());
                    }
                });
        b.add(fileTypeBox);
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.NORTH);

        b = Box.createHorizontalBox();
        b.add(Box.createHorizontalGlue());
        b.add(new JLabel("Data Directory"));
        b.add(Box.createHorizontalStrut(10));
        Text text2 = (Text)XPathAPI.selectSingleNode(element, "dataDirectory/text()");
        b.add(EditorUtil.getTextField(text2));
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.CENTER);

        b = Box.createHorizontalBox();
        b.setBorder(new TitledBorder("Event Directory"));
        b.add(Box.createHorizontalGlue());
        Box innerBox = Box.createHorizontalBox();
        innerBox.setBorder(new TitledBorder("Prefix"));
        text2 = (Text)XPathAPI.selectSingleNode(element, "eventDirLabel/text()");
        innerBox.add(EditorUtil.getTextField(text2));
        b.add(innerBox);
        Box origBox = Box.createHorizontalBox();
        origBox.setBorder(new TitledBorder("Origin Format"));
        text2 = (Text)XPathAPI.selectSingleNode(element, "eventDirLabel/originTime/text()");
        origBox.add(EditorUtil.getTextField(text2));
        b.add(origBox);
        b.add(Box.createHorizontalGlue());
        panel.add(b, BorderLayout.SOUTH);

        return panel;
    }

    public static String[] fileTypes = {"sac", "mseed"};
}

