/**
 * MenuTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.status.AllTypeTemplate;
import java.util.Iterator;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class MenuTemplate extends Template implements GenericTemplate{

    private String pathFrom;
    private String fileDir;

    public MenuTemplate(Element el, String pathFrom, String fileDir) throws ConfigurationException {
        this.pathFrom = pathFrom;
        this.fileDir = fileDir;
        parse(el);
    }

    public Object getTemplate(String tag, final Element el){
        if (tag.equals("relativePath")){
            return new AllTypeTemplate(){
                public String getResult(){
                    Node firstChild = el.getFirstChild();
                    String absPathTo = fileDir + '/' + firstChild.getNodeValue();
                    if(el.getFirstChild() instanceof Element){
                        Element el = (Element)firstChild;
                        absPathTo = ((AllTypeTemplate)getCommonTemplate(el.getNodeName(),
                                                                        el)).getResult();
                    }
                    return SodUtil.getRelativePath(pathFrom, absPathTo, "/");
                }
            };
        }
        return getCommonTemplate(tag, el);
    }

    /**
     *returns an object of the template type that this class uses, and returns
     * the passed in text when the getResult method of that template type is
     * called
     */
    protected Object textTemplate(final String text) {
        return new AllTypeTemplate(){
            public String getResult() {
                return text;
            }
        };
    }

    public String getResult() {
        StringBuffer buf = new StringBuffer();
        Iterator e = templates.iterator();
        while(e.hasNext()) {
            Object cur = e.next();
            buf.append(((GenericTemplate)cur).getResult());
        }
        return buf.toString();
    }


}

