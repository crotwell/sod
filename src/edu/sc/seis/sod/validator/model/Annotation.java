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
    
    public void setDeprecation(String deprecation){
        this.deprecation = deprecation;
    }

    public void setSummary(String summary) {
        this.summary = summary.replaceAll("\n\\s*", " ");
    }

    public void setVelocity(String vel) {
        this.vel = vel;
    }

    public void setExample(String example) {
        this.example = example;
        hasExampleFromAnnotation = true;
    }

    public String getDescription() {
        return desc;
    }

    public String getDeprecation() {
        return deprecation;
    }

    public String getSummary() {
        return summary;
    }

    public String getVelocity() {
        return vel;
    }

    public boolean hasSummary() {
        return summary != null;
    }

    public String getExample() {
        // System.out.println("getting example");
        return getExample(DEFAULT_HTMLIZE);
    }

    public void setFormProvider(FormProvider fp) {
        formProvider = fp;
    }

    public FormProvider getFormProvider() {
        return formProvider;
    }

    public String getExample(boolean htmlize) {
        if(example == null || example.equals("")) {
            XMLWritingTourist tourist = new XMLWritingTourist();
            TourGuide guide = new MinimalVisitGuide(formProvider.getForm());
            guide.lead(tourist);
            example = tourist.getResult();
        }
        if(htmlize) {
            return getHTMLizedString(example);
        }
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

    public Annotation makeCopyWithNewFormProvider(FormProvider fp) {
        Annotation copy = new Annotation();
        copy.summary = summary;
        copy.desc = desc;
        copy.deprecation = deprecation;
        copy.hasExampleFromAnnotation = hasExampleFromAnnotation;
        copy.example = example;
        copy.include = include;
        copy.vel = vel;
        copy.setFormProvider(fp);
        return copy;
    }

    private boolean hasExampleFromAnnotation = false;

    private String summary, desc, example, vel = "#ingredientPage()", deprecation;

    private FormProvider formProvider;

    private boolean include = false;

    public static boolean DEFAULT_HTMLIZE = true;
}