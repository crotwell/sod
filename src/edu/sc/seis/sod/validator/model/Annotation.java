/**
 * Annotation.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Annotation{
    public void setDescription(String description){ desc = description; }

    public void setSummary(String summary){ this.summary = summary; }

    public void setExample(String example){
        this.example = example;
    }

    public String getDescription(){ return desc; }

    public String getSummary(){ return summary; }

    public String getExample(){
        return getExample(DEFAULT_HTMLIZE);
    }

    public void setFormProvider(FormProvider fp){
        formProvider = fp;
    }

    public String getExample(boolean htmlize){
        if (example.equals("")){
            ExampleBuilder eb = new ExampleBuilder(htmlize);
            htmlize = false; //since the exampleBuilder handles htmlization, we don't need to
            eb.write(formProvider.getForm());
            example = eb.toString();
        }
        if (htmlize){
            return getHTMLizedString(example);
        }
        else {
            return example;
        }
    }

    public static String getHTMLizedString(String bracketedXML){
        StringBuffer buf = new StringBuffer();
        char[] chars = bracketedXML.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '<'){
                buf.append("&lt;");
            } else if (chars[i] == '>'){
                buf.append("&gt;");
            } else {
                buf.append(chars[i]);
            }
        }
        return buf.toString();
    }

    private String summary, desc;
    private String example = "";
    private FormProvider formProvider;

    public static boolean DEFAULT_HTMLIZE = true;
}

