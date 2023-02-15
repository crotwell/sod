package edu.sc.seis.sod.util.streampump;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author oliverpa Created on Jan 21, 2005
 */
public class GenericCommandExecute {

    public static int execute(String command) throws InterruptedException,
            IOException {
        return execute(command, new StringReader(""), System.out, System.err);
    }

    public static int execute(String command,
                              Reader stdin,
                              OutputStream stdout,
                              OutputStream stderr) throws InterruptedException,
            IOException {
        return execute(command,
                       stdin,
                       new OutputStreamWriter(stdout),
                       new OutputStreamWriter(stderr));
    }

    public static int execute(String command,
                              Reader stdin,
                              Writer stdout,
                              Writer stderr) throws InterruptedException,
            IOException {
        /* ... new way...
        ProcessBuilder pb =
                new ProcessBuilder("myCommand", "myArg1", "myArg2");
              Map<String, String> env = pb.environment();
              env.put("VAR1", "myValue");
              env.remove("OTHERVAR");
              env.put("VAR2", env.get("VAR1") + "suffix");
              pb.directory(new File("myDir"));
              File log = new File("log");
              pb.redirectErrorStream(true);
              pb.redirectOutput(Redirect.appendTo(log));
              Process p = pb.start();
              assert pb.redirectInput() == Redirect.PIPE;
              assert pb.redirectOutput().file() == log;
              assert p.getInputStream().read() == -1;
        */
        
        Runtime rt = Runtime.getRuntime();
        // System.out.println("executing command: " + command);
        Process proc = rt.exec(command);
        Reader reader = new InputStreamReader(proc.getInputStream());
        // BufferedWriter writer = new BufferedWriter(stdout);
        Writer inWriter = new OutputStreamWriter(proc.getOutputStream());
        Reader errReader = new InputStreamReader(proc.getErrorStream());
        // BufferedWriter errWriter = new BufferedWriter(stderr);
        StreamPump pump = new StreamPump(reader, stdout, false);
        pump.setName("stdout");
        pump.start();
        StreamPump errPump = new StreamPump(errReader, stderr, false);
        errPump.setName("stderr");
        errPump.start();
        StreamPump stdInPump = new StreamPump(stdin, inWriter, true);
        stdInPump.setName("stdin");
        stdInPump.start();
        int exitVal = proc.waitFor();
        // waiting for finish of StreamPump runs
        try {
            pump.join();
        } catch(InterruptedException e) {
            // assume all is well???
        }
        try {
            errPump.join();
        } catch(InterruptedException e) {
            // assume all is well???
        }
        try {
            stdInPump.join();
        } catch(InterruptedException e) {
            // assume all is well???
        }
        if(!pump.hasCompleted() || !errPump.hasCompleted()) {
            throw new IllegalStateException("Pumps must complete: stdout:"
                    + pump.hasCompleted() + "  stderr:"
                    + errPump.hasCompleted() + "  exitValue:" + exitVal);
        }
        return exitVal;
    }

    public static void main(String[] args) {
        try {
            if(args.length > 0) {
                execute(args[0]);
            } else {
                execute("ls /bin");
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private static Logger logger = LoggerFactory.getLogger(GenericCommandExecute.class);
}
