/**
 * Version.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.sod;

public class Version{

    private int dbid;
    private String version;
    private boolean schemaChange;

    /** for hibernate */
    protected Version() {}
    
    public String getVersion() {
        return version;
    }

    
    protected void setVersion(String version) {
        this.version = version;
    }

    
    public boolean isSchemaChange() {
        return schemaChange;
    }

    protected void setSchemaChange(boolean schemaChange) {
        this.schemaChange = schemaChange;
    }

    public int getDbid() {return dbid;}
    public void setDbid(int dbid) {
        this.dbid = dbid;
    }
    
    public Version(String version, boolean schemaChange) {
        this.version = version;
        this.schemaChange = schemaChange;
    }

    public static Version current() {
        return versionsToDate[versionsToDate.length-1];
    }
    
    public static boolean hasSchemaChangedSince(String version){
        if (!current().getVersion().equals(version)){
            boolean found = false;
            boolean schemaChanged = false;
            for (int i = 0; i < versionsToDate.length; i++) {
                if (found){
                    if (versionsToDate[i].isSchemaChange()){
                        schemaChanged = true;
                        break;
                    }
                }
                if (versionsToDate[i].getVersion().equals(version)){
                    found = true;
                }
            }
            return schemaChanged;
        }
        else {
            return false;
        }
    }

    public static String getVersionDate() { return datetime; }

    public static String getCVSVersion() { return cvsversion; }

    private static String cvsversion = "$Revision: 20746 $";
    private static String datetime = "$Date: 2009-09-24 15:55:11 -0400 (Thu, 24 Sep 2009) $";

    //versionsToDate stores all of the versions that have been released,
    //and is a multidimensional array of width 2.  The first String in
    //an entry is the version number, and the second states whether or
    //not there has been a schema change in that release.
    public static Version[] versionsToDate = {
      new Version("2.0beta1", true),
      new Version("2.0beta2", true),
      new Version("2.0beta3", true),
      new Version("2.0beta4", true),  //suspended pairs, new channels
      new Version("2.0beta5", false),  //small bugfixes
      new Version("2.0beta6", false),   //even smaller bugfixes
      new Version("2.0beta7", false),   //status table sorting, status beautification, editor load/save, legacy exec, PointLinearDistanceMagnitude
      new Version("2.0beta8", false),   
      new Version("2.0rc1", true),   
      new Version("2.0", false),
      new Version("2.1", false),
      new Version("2.1.1", false),
      new Version("2.1.2rc1", true),
      new Version("2.1.2rc2", false),
      new Version("2.2rc1", false),
      new Version("2.2", false),
      new Version("2.2.1beta1", false),
      new Version("2.2.1", false),
      new Version("2.2.2", false),
      new Version("3.0alpha", true),
      new Version("3.0beta1", true),
      new Version("3.0beta2", true),
      new Version("3.0beta3", true)
    };

}



