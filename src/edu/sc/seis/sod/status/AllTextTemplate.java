/**
 * AllTextTemplate.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.status;

public class AllTextTemplate extends AllTypeTemplate
{
    public AllTextTemplate(String text) {
        this.text = text;
    }

    /**
     * Method getResult
     *
     * @return   a String
     *
     */
    public String getResult() {
        return text;
    }


    protected String text;
}

