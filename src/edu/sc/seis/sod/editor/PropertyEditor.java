/**
 * PropertyEditor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;
import javax.swing.JComponent;
import org.w3c.dom.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import org.apache.xpath.XPathAPI;
import javax.xml.transform.TransformerException;
import java.util.Properties;
import java.io.IOException;
import java.awt.event.*;



public class PropertyEditor implements EditorPlugin
{
	
    public JComponent getGUI(Element element) throws TransformerException
	{
		JPanel panel = new JPanel();
		try
		{
			props.load((PropertyEditor.class).getClassLoader().getResourceAsStream( "properties.prop" ));
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.out.println("Error in loading properties file in Property Editor");
		}
		if (element.getTagName().equals("property"))
		{
			panel.setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx=0;
			gbc.gridy=0;
			gbc.fill = gbc.HORIZONTAL;
			gbc.weightx = 1;
			gbc.weighty = 1;
			Node node = XPathAPI.selectSingleNode(element, "name/text()");
			Text text = (Text)node;
			JLabel label;
			String labeltext = null;
			StringTokenizer st = new StringTokenizer(text.getNodeValue(),".");
			while(st.hasMoreTokens())
				labeltext = st.nextToken();
			
			label = new JLabel(labeltext+" ");
				
			label.setHorizontalTextPosition(SwingConstants.RIGHT);
			panel.add(label, gbc);
			gbc.gridx++;
			JComboBox combo = null;
			
			if(labeltext.equals("getNewEvents"))
			combo =	initComboBox(element,flags);
							
			else if ( labeltext.equals("daystoincrement"))
			{
				//for(int j=1;j<=100;j++)
					//vals[j]= (new Integer(j)).toString();
				combo = initComboBox(element,numVals);
			}
			else
				combo = initComboBox(element,defaultVals);
			panel.add(combo, gbc);
			gbc.gridx--;
			
		}
		return panel;
    }
	public JComboBox initComboBox(Element element, String[] vals) throws TransformerException {
        Node node = XPathAPI.selectSingleNode(element, "value/text()");
        Text text = (Text)node;
        JComboBox combo = new JComboBox(vals);
        combo.addItem(text.getNodeValue());
        combo.setSelectedItem(text.getNodeValue());
        combo.addItemListener(new TextItemListener(text));
        return combo;
    }
	class TextItemListener implements ItemListener {
        TextItemListener(Text text) {
            this.text = text;
        }
        Text text;
        public void itemStateChanged(ItemEvent e) {
            Object item = ((JComboBox)e.getSource()).getSelectedItem();
            text.setNodeValue((String)item);
        }
    }
	Properties props = new Properties();
	String[] flags = {"true","false"};
	String[] vals;
	String[] numVals = {"1","2"};
	String[] defaultVals = {" "};
}


