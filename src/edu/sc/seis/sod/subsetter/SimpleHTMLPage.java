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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SimpleHTMLPage{
        public SimpleHTMLPage(String title, String fileName, File path){
            this.title = title;
            this.path = path;
            this.fileName = fileName;
        }
        
        public void append(String section, String text){
            if(sectionToContents.get(section) == null){
                sectionToContents.put(section, text + "<br>\n");
            }else{
                String curContents = (String)sectionToContents.get(section);
                sectionToContents.put(section, curContents + text + "<br>\n");
            }
        }
        
        public void clear(String section){
            sectionToContents.remove(section);
        }
        
        public void addLink(SimpleHTMLPage page){
            append("Links", "<A HREF=" + page + ">" + page.getTitle() + "</A>");
        }
        
        public void addLinks(SimpleHTMLPage[] pages){
            for (int i = 0; i < pages.length; i++) {
                addLink(pages[i]);
            }
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
            String body = "<body>\n";
            body += constructContents();
            body += "</body>\n</html>";
            return header + body;
        }
        
        private String constructContents(){
            String contents = "";
            Iterator it = sectionToContents.keySet().iterator();
            while(it.hasNext()){
                String title = (String)it.next();
                String body = (String)sectionToContents.get(title);
                contents += "<b>" + title +":</b><br>\n";
                contents += body;
            }
            return contents;
        }
        
        private Map sectionToContents = new HashMap();
        
        private File path;
        
        private String fileName, title;
}

