/**
 * JDBCVelocityContext.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.database.waveform;

import java.io.Serializable;

import org.apache.velocity.context.AbstractContext;
import org.apache.velocity.context.Context;

import edu.sc.seis.sod.EventChannelPair;
import edu.sc.seis.sod.hibernate.EcpCookie;
import edu.sc.seis.sod.hibernate.SodDB;

public class JDBCVelocityContext extends AbstractContext {

    public JDBCVelocityContext(EventChannelPair ecp, Context context) {
        super(context);
        this.ecp = ecp;
        this.soddb = SodDB.getSingleton();
    }

    public Object internalGet(String name) {
        EcpCookie cookie = soddb.getCookie(ecp, name);
        if(cookie == null) {
            return null;
        }
        if(cookie.getValueString() != null) {
            return cookie.getValueString();
        }
        if(cookie.getValueObject() != null) {
            return cookie.getValueObject();
        }
        return new Double(cookie.getValueDouble());
    }

    /**
     * The interface requires Object value, but we really want it to be limited
     * to String Double and Serializable.
     */
    public Object internalPut(String name, Object value) {
        if (value instanceof Serializable) {
            EcpCookie cookie = new EcpCookie(ecp, name, (Serializable)value);
            soddb.putCookie(cookie);
            SodDB.commit();
            return cookie;
        } else {
            throw new IllegalArgumentException("value must be a String or a Double or a Serializable: "+value.getClass().getName());
        }
        
    }

    public boolean internalContainsKey(Object name) {
        return soddb.getCookie(ecp, (String)name) == null;
    }

    public Object[] internalGetKeys() {
        return soddb.getCookieNames(ecp).toArray(new String[0]);
    }

    public Object internalRemove(Object name) {
        EcpCookie cookie = soddb.getCookie(ecp, (String)name);
        soddb.deleteCookie(cookie);
        return cookie;
    }

    EventChannelPair ecp;

    SodDB soddb;
}
