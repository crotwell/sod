package edu.sc.seis.sod.retry;

public abstract class BaseRetryStrategy implements RetryStrategy {
    
    public BaseRetryStrategy(int numRetries) {
        this.numRetries = numRetries;
    }

    public abstract boolean shouldRetry(Throwable exc,
                                        Object server,
                                        int tryCount);

    protected boolean basicShouldRetry(Throwable exc,
                                       Object server,
                                       int tryCount) {
        if (numRetries == -1 || tryCount < numRetries) {
            retrySleep(tryCount);
            return true;
        } else {
            return false;
        }
    }
    
    public void serverRecovered(Object server){}
    


    /**
     * Sleep for some time between retries. Each RetryXYZDC proxy uses this to
     * retry less frequently as the number of failures in a row increases.
     */
    public static void retrySleep(int count) {
        if(count > 1) {
            try {
                if(count > 30) {
                    Thread.sleep((defaultTimeoutSeconds+ 300)* sleepSeconds * 1000);
                } else if(count > 10) {
                        Thread.sleep((defaultTimeoutSeconds+ 10 * count) * sleepSeconds * 1000);
                } else {
                    Thread.sleep(defaultTimeoutSeconds * sleepSeconds * 1000);
                }
            } catch(InterruptedException e) {
                // oh well
            }
        }
    }
    
    public static int getDefaultNumRetry() {
        return defaultNumRetry;
    }

    public static void setDefaultNumRetry(int defaultNum) {
        defaultNumRetry = defaultNum;
    }

    
    int numRetries;
    
    protected static int defaultTimeoutSeconds = 20;
    
    protected static int sleepSeconds = 1;

    private static int defaultNumRetry = 3;
}
