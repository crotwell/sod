/**
 * Extractor.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.util.exceptionHandler;

/** Extractor provides a way to extract important information from a
 * Throwable that should be in the exception report, but is either not
 * in the toString, or is difficult to find in the toString. This also
 * allows the GlobalExceptionHandler to not be encombered with alot of
 * exception specific code. */
public interface Extractor {

    /**Should return true if this extractor is capable of understanding
     * this type of Throwable. Typically, this will be done via code like
     * if (throwable instanceof MyException) { return true; }
     * */
    public boolean canExtract(Throwable throwable);

    /** Extracts a string version of the throwable. */
    public String extract(Throwable throwable);

    /** gets a Wrapped exception, if it exists. */
    public Throwable getSubThrowable(Throwable throwable);

}

