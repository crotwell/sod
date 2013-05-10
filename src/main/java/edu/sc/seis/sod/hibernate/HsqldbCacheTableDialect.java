package edu.sc.seis.sod.hibernate;

import org.hibernate.dialect.HSQLDialect;


public class HsqldbCacheTableDialect extends HSQLDialect {
    

    public String getCreateTableString() {
        return "create cached table";
    }
}
