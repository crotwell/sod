/**
 * PageLinkSection.java
 *
 * @author Created by Charles Groves
 */

package edu.sc.seis.sod.subsetter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LinkSection extends PageSection{
    public LinkSection(SimpleHTMLPage owner){ this(owner, "Links"); }
    
    public LinkSection(SimpleHTMLPage owner, String name){
        this(owner, name, null);
    }
    
    public LinkSection(SimpleHTMLPage owner, String name, List pageList){
        super(name);
        this.owner = owner;
        if(pageList != null) this.pages = pageList;
    }
    
    public void add(SimpleHTMLPage page){
        pages.add(page);
    }
    
    public String getContents(){
        StringBuffer contents = new StringBuffer("");
        synchronized(pages){
            Iterator it = pages.iterator();
            while(it.hasNext()){
                SimpleHTMLPage page = (SimpleHTMLPage)it.next();
                if(page != owner){
                    contents.append("<A HREF=" + owner.relativePathTo(page) + ">" + page.getTitle() + "</A><br>\n");
                }
            }
        }
        return contents.toString();
    }
    
    private List pages = Collections.synchronizedList(new ArrayList());
    
    private SimpleHTMLPage owner;
}
