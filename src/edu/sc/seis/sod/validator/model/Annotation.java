/**
 * Annotation.java
 * 
 * @author Charles Groves
 */
package edu.sc.seis.sod.validator.model;

import edu.sc.seis.sod.validator.tour.MinimalVisitGuide;
import edu.sc.seis.sod.validator.tour.TourGuide;
import edu.sc.seis.sod.validator.tour.XMLWritingTourist;

public class Annotation {

    public void setDescription(String description) {
        desc = description;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setExample(String example) {
        this.example = example;
        hasExampleFromAnnotation = true;
    }

    public String getDescription() {
        Form f = getFormProvider().getForm();
        if(desc == null && f.isFromDef() && f instanceof MultigenitorForm) {
            Definition def = f.getDef();
            return "This contains all of the "
                    + def.getName()
                    + " tactics.  It is not a real "
                    + def.getName()
                    + " tactic.  Instead of using this directly in the strategy file, use one of the tactics listed in the 'This consists of' section of this page.";
        }
        return desc;
    }

    public String getSummary() {
        return summary;
    }

    public String getExample() {
        //System.out.println("getting example");
        return getExample(DEFAULT_HTMLIZE);
    }

    public void setFormProvider(FormProvider fp) {
        formProvider = fp;
    }

    public FormProvider getFormProvider() {
        return formProvider;
    }

    public String getExample(boolean htmlize) {
        if(example.equals("") || example == null) {
            XMLWritingTourist tourist = new XMLWritingTourist();
            TourGuide guide = new MinimalVisitGuide(formProvider.getForm());
            guide.lead(tourist);
            example = tourist.getResult();
        }
        if(htmlize) { return getHTMLizedString(example); }
        return example;
    }

    public boolean hasExampleFromAnnotation() {
        return hasExampleFromAnnotation;
    }

    public void setInclude(boolean include) {
        this.include = include;
    }

    public boolean getInclude() {
        return include;
    }

    public static String getHTMLizedString(String bracketedXML) {
        StringBuffer buf = new StringBuffer(bracketedXML);
        String newString = replaceAllInstances(buf.toString(), "<", "&lt;");
        buf = new StringBuffer(newString);
        newString = replaceAllInstances(buf.toString(), ">", "&gt;");
        return newString;
    }

    public static String replaceAllInstances(String string,
                                             String orig,
                                             String replacement) {
        StringBuffer buf = new StringBuffer(string);
        int repIndex = buf.indexOf(orig);
        while(repIndex != -1) {
            buf.replace(repIndex, repIndex + 1, replacement);
            repIndex = buf.indexOf(orig);
        }
        return buf.toString();
    }

    private boolean hasExampleFromAnnotation = false;

    private String summary, desc;

    private String example = "";

    private FormProvider formProvider;

    private boolean include = false;

    public static boolean DEFAULT_HTMLIZE = true;
}