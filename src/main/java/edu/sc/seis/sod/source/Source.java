package edu.sc.seis.sod.source;

public interface Source {

    /**
     * returns the DNSName of the server.
     * The context under which the objectName is registered in the CORBA naming service.
     * 
     * For non-corba servers, this string can be anything that seems reasonable and is relatively unique
     *
     * @return a <code>String</code> value
     */
    public abstract String getDNS();

    /**
     * returns the sourceName of the server. The name to which the server's servant instance is bound
     * in the CORBA naming service.
     *
     * For non-corba servers, this string can be anything that seems reasonable and is relatively unique
     * 
     * @returns a <code>String</code> value
     */
    public abstract String getName();
}