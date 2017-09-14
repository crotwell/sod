/**
 * StAXFileWriter.java
 * 
 * @author Philip Oliver-Paull Convenience class for StAX stream writing
 */
package edu.sc.seis.sod;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StAXFileWriter {

    public StAXFileWriter(File file) throws IOException, XMLStreamException {
        outFile = file;
        if(outFile.exists()) {
            tempFile = File.createTempFile("Temp_" + outFile.getName(),
                                           null,
                                           outFile.getAbsoluteFile()
                                                   .getParentFile());
            isTempFiled = true;
        } else {
            tempFile = outFile;
        }
        fileWriter = new BufferedOutputStream(new FileOutputStream(tempFile));
        xmlWriter = XMLUtil.getStaxOutputFactory().createXMLStreamWriter(fileWriter,
                                                                    "UTF-8");
    }

    public XMLStreamWriter getStreamWriter() {
        return xmlWriter;
    }

    public synchronized void abort() throws XMLStreamException, IOException {
        logger.debug("abort called");
        xmlWriter.close();
        fileWriter.close();
        tempFile.delete();
    }

    public synchronized void close() throws XMLStreamException, IOException {
        if(!isClosed) {
            xmlWriter.writeEndDocument();
            xmlWriter.close();
            fileWriter.close();
            if(isTempFiled) {
                if(!tempFile.renameTo(outFile.getAbsoluteFile())) {
                    logger.debug("Unable to rename " + tempFile + " to "
                            + outFile.getAbsoluteFile());
                    // If unable to rename the tempfile, delete it and try again
                    if(outFile.delete()) {
                        tempFile.renameTo(outFile);
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch(InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                        if(outFile.delete()) {
                            tempFile.renameTo(outFile);
                        } else {
                            throw new IOException("Unable to move temp file over old file");
                        }
                    }
                } else {
                    logger.debug("Renamed " + tempFile + " to " + outFile);
                }
            }
            isClosed = true;
        }
    }

    private File outFile, tempFile;

    private boolean isTempFiled = false;

    private boolean isClosed = false;

    private OutputStream fileWriter;

    private XMLStreamWriter xmlWriter;

    private Logger logger = LoggerFactory.getLogger(StAXFileWriter.class);
}
