/**
 * Version.java
 *
 * @author Philip Oliver-Paull
 */

package edu.sc.seis.sod.model.common;

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dbid;
        result = prime * result + (schemaChange ? 1231 : 1237);
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Version other = (Version) obj;
        if (dbid != other.dbid)
            return false;
        if (schemaChange != other.schemaChange)
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

    
}



