/**
 * Annotation.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.ModelWalker;

public class Annotation{
    public void setDescription(String description){ desc = description; }

    public void setSummary(String summary){ this.summary = summary; }

    public void setExample(String example){
        this.example = example;
    }

    public String getDescription(){ return desc; }

    public String getSummary(){ return summary; }

    public String getExample(){
        //System.out.println("getting example");
        return getExample(DEFAULT_HTMLIZE);
    }

    public void setFormProvider(FormProvider fp){
        formProvider = fp;
    }

    public String getExample(boolean htmlize){
        //System.out.println("getExample(" + htmlize + ")");
        if (example.equals("") || example == null){
            ExampleBuilder eb = new ExampleBuilder(htmlize);
            //System.out.println("created new ExampleBuilder");
            eb.write(formProvider.getForm());
            //System.out.println("wrote example");
            example = eb.toString();
            //System.out.println("got example");
            //System.out.println(example);
        }
        else if (htmlize){
            //System.out.println("htmlize");
            String string = getHTMLizedString(example);
            //System.out.println("htmlized existed example");
            System.out.println(string);
            return string;
        }
        //System.out.println("blah");
        return example;
    }

    public boolean hasExample(){
        return !(example.equals("") || example == null);
    }

    public void setInclude(boolean include){
        this.include = include;
    }

    public boolean getInclude(){
        return include;
    }

    public static String getHTMLizedString(String bracketedXML){
        StringBuffer buf = new StringBuffer(bracketedXML);
        String newString = replaceAllInstances(buf.toString(), "<", "&lt;");
        buf = new StringBuffer(newString);
        newString = replaceAllInstances(buf.toString(), ">", "&gt;");
        return newString;
    }

    public static String replaceAllInstances(String string, String orig, String replacement){
        StringBuffer buf = new StringBuffer(string);
        int repIndex = buf.indexOf(orig);
        while (repIndex != -1){
            buf.replace(repIndex, repIndex + 1, replacement);
            repIndex = buf.indexOf(orig);
        }
        return buf.toString();
    }

    private String summary, desc;
    private String example = "";
    private FormProvider formProvider;
    private boolean include = false;

    public static boolean DEFAULT_HTMLIZE = true;
}

