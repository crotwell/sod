/**
 * Version.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.sod;

public class Version{
    private static String cvsversion = "$Revision: 8664 $";
    private static String datetime = "$Date: 2004-05-11 14:36:58 -0400 (Tue, 11 May 2004) $";
    private static String state = "$State$";
    private static String version = "2.0beta1";

    public static String getVersion() { return version; }

    public static String getVersionDate() { return datetime; }

    public static String getCVSVersion() { return cvsversion; }
}

