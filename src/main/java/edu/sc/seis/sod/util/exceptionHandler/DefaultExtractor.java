/**
 * DefaultExtractor.java
 * 
 * @author Created by Omnicore CodeGuide
 */
package edu.sc.seis.sod.util.exceptionHandler;

import java.sql.SQLException;

import edu.sc.seis.sod.model.common.FissuresException;

public class DefaultExtractor implements Extractor {

    public boolean canExtract(Throwable throwable) {
        return true;
    }

    public String extract(Throwable throwable) {
        String traceString = "";
        if(throwable instanceof SQLException) {
            traceString += "SQLState: "
                    + ((SQLException)throwable).getSQLState() + '\n';
            traceString += "Vendor code: "
                    + ((SQLException)throwable).getErrorCode() + '\n';
        }
        return traceString;
    }

    public Throwable getSubThrowable(Throwable throwable) {
        return null;
    }
}
