/**
 * Annotation.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator.model;

public class Annotation{
    public void setDescription(String description){ desc = description; }

    public void setSummary(String summary){ this.summary = summary; }

    public String getDescription(){ return desc; }

    public String getSummary(){ return summary; }

    private String summary, desc;
}

