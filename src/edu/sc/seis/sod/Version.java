/**
 * Version.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.sod;

public class Version{

    public static String getVersion() {
        return versionsToDate[versionsToDate.length - 1][0];
    }

    public static boolean versionHasSchemaChange(){
        return getBooleanValue(versionsToDate[versionsToDate.length - 1][1]);
    }

    public static boolean hasSchemaChangedSince(String version){
        if (!getVersion().equals(version)){
            boolean found = false;
            boolean schemaChanged = false;
            for (int i = 0; i < versionsToDate.length; i++) {
                if (found){
                    if (getBooleanValue(versionsToDate[i][1])){
                        schemaChanged = true;
                        break;
                    }
                }
                if (versionsToDate[i][0].equals(version)){
                    found = true;
                }
            }
            return schemaChanged;
        }
        else {
            return false;
        }
    }

    private static boolean getBooleanValue(String value){
        return (new Boolean(value)).booleanValue();
    }

    public static String getVersionDate() { return datetime; }

    public static String getCVSVersion() { return cvsversion; }

    private static String cvsversion = "$Revision: 8961 $";
    private static String datetime = "$Date: 2004-05-27 16:57:02 -0400 (Thu, 27 May 2004) $";
    private static String state = "$State$";

    //versionsToDate stores all of the versions that have been released,
    //and is a multidimensional array of width 2.  The first String in
    //an entry is the version number, and the second states whether or
    //not there has been a schema change in that release.
    public static String[][] versionsToDate = {
      {"2.0beta1", "true"},
      {"2.0beta2", "false"},
      {"2.0beta3", "false"}
    };

//    public static String[][] versionsToDate = {
//        {"2.0beta1", "true"}
//    };
}

