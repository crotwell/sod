/**
 * 
 */
package edu.sc.seis.sod;

import java.time.Instant;

public interface MicroSecondDateSupplier {

    public Instant load();
}