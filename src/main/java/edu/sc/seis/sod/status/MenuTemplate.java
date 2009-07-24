/**
 * MenuTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;

public class MenuTemplate extends Template implements GenericTemplate{

    private String pathFrom;
    private String fileDir;

    public static final String TEMPLATE_LOC = "jar:edu/sc/seis/sod/data/templates/menu.xml";

    public MenuTemplate(Element el, String pathFrom, String fileDir) throws ConfigurationException {
        this.pathFrom = pathFrom;
        this.fileDir = fileDir;
        parse(el);
    }

    public Object getTemplate(String tag, Element el){
        if (tag.equals("relativePath")){ return new RelativePath(el, pathFrom);}
        return getCommonTemplate(tag, el);
    }

    public class RelativePath extends AllTypeTemplate{
        public RelativePath(Element el, String pathFrom){
            Node firstChild = el.getFirstChild();
            String absPathTo = fileDir + '/' + firstChild.getNodeValue();
            if(el.getFirstChild() instanceof Element){
                el = (Element)firstChild;
                absPathTo = fileDir + '/' + ((GenericTemplate)getCommonTemplate(el.getNodeName(),
                                                                                el)).getResult();
            }
            path = SodUtil.getRelativePath(pathFrom, absPathTo, "/");
        }

        public String getResult() { return path; }

        private String path;
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

