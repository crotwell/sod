/**
 * Pattern.java
 *
 * @author Charles Groves
 */

package edu.sc.seis.sod.validator;

public interface Pattern{
    public void addChild(Pattern child);

    public void addChildren(Pattern[] children);

    public Pattern[] getKids();

    public Pattern getParent();

    public void dereference();
}

