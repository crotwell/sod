package edu.sc.seis.sod.hibernate;

import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.fdsnws.stationxml.Response;
import edu.sc.seis.sod.status.FissuresFormatter;

import java.io.*;

/**
 * Get and save channel instrumentation from file, in messagepack format.
 */
public class InstrumentationDB {

    private final File dbdir;

    public InstrumentationDB(File dbdir) {
        this.dbdir = dbdir;
    }

    public File save(Channel channel, Response response) throws IOException {
        File instFile = instFile(channel);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(instFile));
        // if response is null, write empty file to signify we tried but no response exists.
        if (response != null) {
            out.write(InstrumentationBlob.getResponseAsBlob(channel, response));
        }
        out.close();
        return instFile;
    }

    public Response load(Channel channel) throws IOException, ChannelNotFound {
        File instFile = instFile(channel);
        if ( ! instFile.exists()) {
            return null;
        }
        BufferedInputStream out = new BufferedInputStream(new FileInputStream(instFile));
        byte[] instBytes = out.readAllBytes();
        Response response = null;
        if (instBytes.length == 0) {
            // tried to load previously, but no response exists, return null;
            throw new ChannelNotFound("File "+instFile.toString()+" is empty.", channel);
        } else {
            response = InstrumentationBlob.getResponseFromBlob(instBytes);
        }
        return response;
    }

    public File instFile(Channel channel) {
        File netDir = new File(dbdir, channel.getNetworkId());
        if (! netDir.exists()) {
            netDir.mkdirs();
        }
        File staDir = new File(netDir, channel.getStationCode());
        if ( ! staDir.exists()) {
            staDir.mkdirs();
        }
        String chanFilename = FissuresFormatter.filize(
                channel.getNetworkCode()
                        +"_"+channel.getStationCode()
                        +"_"+channel.getLocCode()
                        +"_"+channel.getChannelCode()
                        +"_"+channel.getStartDate());

        File instFile = new File(staDir, chanFilename);
        return instFile;
    }
}
