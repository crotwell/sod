/**
 * HTMLPage.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.subsetter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SimpleHTMLPage{
    public SimpleHTMLPage(String title, String fileName, File path){
        this.title = title;
        this.path = path;
        this.fileName = fileName;
        allSimplePages.add(this);
    }
    
    public void append(String sectionName, String text){
        append(sectionName, text, -1);
    }
    
    public void append(String name, String text, int pos){
        Section sec = getSection(name);
        if(sec == null){
            if(pos < 0)
                sections.add(sections.size(),new Section(name, text + "<br>\n"));
            else
                sections.add(pos, new Section(name, text + "<br>\n"));
        }else{
            sec.append(text + "<br>\n");
        }
    }
    
    public void clear(String section){
        if(getSection(section) != null) getSection(section).clear();
    }
    
    public Section getSection(String name){
        Iterator it = sections.iterator();
        while(it.hasNext()){
            Section cur = (Section)it.next();
            if(cur.getName().equals(name)){
                return cur;
            }
        }
        return null;
    }
    
    public String getTitle(){ return title; }
    
    public File write(){
        File output = new File(path, fileName);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(output));
            writer.write(constructPage());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
    
    public String toString(){
        return fileName;
    }
    
    private String constructPage(){
        String header = "<html>\n<header><title>" + title + "</title></header>\n";
        StringBuffer body = new StringBuffer("<body>\n");
        body.append(constructSections());
        body.append("</body>\n</html>");
        return header + body;
    }
    
    private String constructSections(){
        Iterator it = sections.iterator();
        StringBuffer contents = new StringBuffer("");
        while(it.hasNext()){
            contents.append(((Section)it.next()).getSection());
        }
        contents.append(links.getSection());
        return contents.toString();
    }
    
    private class Section{
        public Section(String name){
            this(name, "");
        }
        
        public Section(String name, String text){
            this.name = name;
            append(text);
        }
        
        public void clear(){ contents = new StringBuffer(""); }
        
        public void append(String newContents){
            if(contents.toString().equals("")) contents = new StringBuffer(newContents);
            else contents.append(newContents);
        }
        
        public String getName(){ return name; }
        
        public String getContents(){ return contents.toString(); }
        
        public String getSection(){
            return "<b>" + name +":</b><br>\n" + getContents();
        }
        
        public boolean equals(Object other){
            if(other instanceof Section){
                if(((Section)other).getName().equals(name)){
                    return true;
                }
            }
            return false;
        }
        
        private String name;
        
        private StringBuffer contents = new StringBuffer("");
    }
    
    private class LinkSection extends Section{
        public LinkSection(){
            super("Links");
        }
        
        public String getContents(){
            StringBuffer contents = new StringBuffer("");
            synchronized(allSimplePages){
                Iterator it = allSimplePages.iterator();
                while(it.hasNext()){
                    SimpleHTMLPage page = (SimpleHTMLPage)it.next();
                    if(page != SimpleHTMLPage.this){
                        contents.append("<A HREF=" + page + ">" + page.getTitle() + "</A><br>\n");
                    }
                }
            }
            return contents.toString();
        }
        
    }
    
    private Section links = new LinkSection();
    
    private static List allSimplePages = Collections.synchronizedList(new ArrayList());
    
    private List sections = new ArrayList();
    
    private File path;
    
    private String fileName, title;
}
