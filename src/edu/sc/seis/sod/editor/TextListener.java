/**
 * TextListener.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.editor;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Text;
import org.apache.log4j.Logger;

public class TextListener implements DocumentListener {
    Text text;

    TextListener(Text text) {
        this.text = text;
    }
    /**
     * Gives notification that an attribute or set of attributes changed.
     *
     * @param e the document event
     */
    public void changedUpdate(DocumentEvent e) {
        try {
            text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (DOMException ex) {
            logger.error(ex);
        } catch (BadLocationException ex) {
            logger.error(ex);
        }
    }

    /**
     * Gives notification that there was an insert into the document.  The
     * range given by the DocumentEvent bounds the freshly inserted region.
     *
     * @param e the document event
     */
    public void insertUpdate(DocumentEvent e) {
        try {
            text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (DOMException ex) {
            logger.error(ex);
        } catch (BadLocationException ex) {
            logger.error(ex);
        }
    }

    /**
     * Gives notification that a portion of the document has been
     * removed.  The range is given in terms of what the view last
     * saw (that is, before updating sticky positions).
     *
     * @param e the document event
     */
    public void removeUpdate(DocumentEvent e) {
        try {
            text.setData(e.getDocument().getText(0, e.getDocument().getLength()));
        } catch (DOMException ex) {
            logger.error(ex);
        } catch (BadLocationException ex) {
            logger.error(ex);
        }
    }

    private static Logger logger = Logger.getLogger(TextListener.class);

}

