/**
 * StreamPump takes an input stream, and an optional output writer, and reads
 * from the input stream and writes it to the output. It does this in a separate
 * thread, useful also for gobbling the stuff in an input stream that you do not
 * really care about, but need to empty to prevent the streams buffers from
 * filling.
 * 
 * @author Philip Crotwell
 */
package edu.sc.seis.sod.util.streampump;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import edu.sc.seis.sod.util.exceptionHandler.GlobalExceptionHandler;

public class StreamPump extends Thread {

    public StreamPump(InputStream in) {
        this(in, null);
    }

    public StreamPump(Reader in) {
        this(in, null, false);
    }

    public StreamPump(InputStream in, Writer out) {
        this(new InputStreamReader(in), out, false);
    }

    public StreamPump(Reader in, Writer out, boolean closeOnExit) {
        this.in = in;
        this.out = out;
        this.closeOnExit = closeOnExit;
    }

    public synchronized void run() {
        try {
            char[] cbuf = new char[512];
            int numRead = 0;
            while((numRead = in.read(cbuf)) != -1) {
                if(out != null) {
                    out.write(cbuf, 0, numRead);
                }
            }
            if(out != null) {
                out.flush();
            }
        } catch(Throwable e) {
            GlobalExceptionHandler.handle(e);
        } finally {
            if(out != null && closeOnExit) {
                try {
                    out.close();
                } catch(IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        hasCompleted = true;
    }

    public boolean hasCompleted() {
        return hasCompleted;
    }

    private Reader in;

    private Writer out;

    private boolean hasCompleted = false, closeOnExit;
}