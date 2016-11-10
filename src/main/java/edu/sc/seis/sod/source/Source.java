package edu.sc.seis.sod.source;

public interface Source {


    /**
     * returns the name of the server. The name to which the server's servant instance is bound
     * in the CORBA naming service.
     *
     * For non-corba servers, this string can be anything that seems reasonable and is relatively unique
     * 
     * @return a <code>String</code> value
     */
    public abstract String getName();
    
    /** Allows name to be appended to in case of conflicts with other sources. */
    public void appendToName(String suffix);
    
    public int getRetries();
}