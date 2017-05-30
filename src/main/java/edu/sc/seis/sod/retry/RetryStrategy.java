package edu.sc.seis.sod.retry;


/**
 * Used by the Retry wrappers to determine if another try should be made to the
 * server.
 * 
 * The classic Retry handling is encoded in BaseRetryStrategy's baseShouldRetry
 * method. If you'd just like to add some logging on top of that, it'd be a good
 * start. ClassicRetryStrategy does the regular implementation with logging,
 * reset, and waiting.
 * 
 * @author groves
 * 
 * Created on Nov 15, 2006
 */
public interface RetryStrategy {

    public boolean shouldRetry(Throwable exc,
                               Object server,
                               int tryCount);
    
    public void serverRecovered(Object server);
}
