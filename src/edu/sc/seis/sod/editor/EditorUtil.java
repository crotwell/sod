/**
 * EditorUtil.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.JTextField;
import org.w3c.dom.Text;

public class EditorUtil {


    static JTextField getTextField(Text text) {
        JTextField textField = new JTextField();
        textField.setText(text.getNodeValue().trim());
        TextListener textListen = new TextListener(text);
        textField.getDocument().addDocumentListener(textListen);
        return textField;
    }

    static String capFirstLetter(String in) {
        char c = in.charAt(0);
        if ( ! Character.isUpperCase(c)) {
            return (""+c).toUpperCase()+in.substring(1);
        }
        return in;
    }

}

