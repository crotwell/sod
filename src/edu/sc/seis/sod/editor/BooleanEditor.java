/**
 * OREditor.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.editor;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import javax.swing.border.Border;



public class BooleanEditor implements EditorPlugin{
    public BooleanEditor(SodGUIEditor editor){
        this.editor = editor;
    }

    public JComponent getGUI(Element element) throws Exception {
        String name = element.getTagName();
        String type = NOT;
        if(name.indexOf("OR") != -1){
            type = OR;
        }else if(name.indexOf("AND") != -1){
            type = AND;
        }
        if(type == NOT){
            Box b = Box.createHorizontalBox();
            //b.add(new JLabel(NOT + " "));
            Element child = (Element)XPathAPI.selectSingleNode(element, "*");
            b.add(editor.getCompForElement(child));
            b.setBorder(new TitledBorder("Not"));
            return b;
        }else{
            Box b = Box.createVerticalBox();
            JComponent[] comps = editor.getCompsForNodeList(element.getChildNodes());
            for (int i = 0; i < comps.length - 1; i++) {
                b.add(comps[i]);
                Box typeBox = Box.createHorizontalBox();
                typeBox.add(new JLabel(type));
                typeBox.add(Box.createHorizontalGlue());
                b.add(typeBox);
            }
            b.add(comps[comps.length - 1]);
            String ssType = name.substring(0, name.length() - type.length());
            System.out.println(ssType);
            Inserter ins = (Inserter)inserters.get(ssType);
            if (ins == null) {
                ins =new Inserter(ssType, editor);
                inserters.put(ssType, ins);
            }
            b.add(ins.getGUI(element));
            if(type == AND){ b.setBorder(AND_BORDER);}
            else{ b.setBorder(OR_BORDER); }
            return b;
        }
    }

    private static Border AND_BORDER = new TitledBorder("All of these");

    private static Border OR_BORDER = new TitledBorder("Any of these");

    private Map inserters = new HashMap();
    private static final String AND = "AND", OR = "OR", NOT ="Not";
    private SodGUIEditor editor;
}

