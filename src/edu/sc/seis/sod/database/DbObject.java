package edu.sc.seis.sod.database;


/**
 * DbObject.java
 *
 *
 * Created: Tue Oct 22 14:29:49 2002
 *
 * @author <a href="mailto:">Srinivasa Telukutla</a>
 * @version
 */

public class DbObject {
    public DbObject (int dbid){ this.dbid = dbid; }

    public int getDbId() { return this.dbid; }

    private int dbid;
}// DbObject
