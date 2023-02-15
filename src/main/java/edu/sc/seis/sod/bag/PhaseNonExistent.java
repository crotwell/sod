/**
 * PhaseNotExist.java
 *
 * @author Created by Omnicore CodeGuide
 */

package edu.sc.seis.sod.bag;

public class PhaseNonExistent extends Exception {
    PhaseNonExistent(String message) {
        super(message);
    }
    
    PhaseNonExistent(String message, Exception cause) {
        super(message, cause);
    }
}

