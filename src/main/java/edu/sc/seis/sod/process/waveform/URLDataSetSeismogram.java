package edu.sc.seis.sod.process.waveform;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.iris.dmc.seedcodec.B1000Types;
import edu.iris.dmc.seedcodec.CodecException;
import edu.sc.seis.seisFile.fdsnws.stationxml.Channel;
import edu.sc.seis.seisFile.mseed.DataRecord;
import edu.sc.seis.seisFile.mseed.SeedFormatException;
import edu.sc.seis.seisFile.mseed.SeedRecord;
import edu.sc.seis.seisFile.sac.SacTimeSeries;
import edu.sc.seis.sod.hibernate.SeismogramFileTypes;
import edu.sc.seis.sod.hibernate.UnsupportedFileTypeException;
import edu.sc.seis.sod.model.common.FissuresException;
import edu.sc.seis.sod.model.event.CacheEvent;
import edu.sc.seis.sod.model.event.NoPreferredOrigin;
import edu.sc.seis.sod.model.seismogram.LocalSeismogramImpl;
import edu.sc.seis.sod.model.station.ChannelIdUtil;
import edu.sc.seis.sod.util.convert.mseed.FissuresConvert;
import edu.sc.seis.sod.util.convert.sac.FissuresToSac;
import edu.sc.seis.sod.util.convert.sac.SacToFissures;

@Deprecated
public class URLDataSetSeismogram {
    
    public static LocalSeismogramImpl getSeismogram(File file, SeismogramFileTypes fileType) throws UnsupportedFileTypeException, FissuresException, IOException, SeedFormatException {
        if(fileType.equals(SeismogramFileTypes.MSEED)) {
            DataInputStream dis = null;
            List<DataRecord> list = new ArrayList<DataRecord>();
            try {
                
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                while(true) {
                    SeedRecord sr = SeedRecord.read(dis, 4096);
                    if (sr instanceof DataRecord) {
                        list.add((DataRecord)sr);
                    }
                }
            } catch(EOFException e) {
                // must be all
            } finally {
                if(dis != null) {
                    dis.close();
                }
            }
            return FissuresConvert.toFissures(list.toArray(new DataRecord[0]), (byte)B1000Types.STEIM1, (byte)1);
        } else if(SeismogramFileTypes.SAC.equals(fileType)) {
            SacTimeSeries sacTime;
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
                sacTime = SacTimeSeries.read(dis);
            } finally {
                if(dis != null) {
                    dis.close();
                }
            }
            return SacToFissures.getSeismogram(sacTime);
        } else {
            throw new UnsupportedFileTypeException("File type "
                    + fileType.getName() + " is not supported");
        }
    }


    public static File saveAsSac(LocalSeismogramImpl seis, File directory)
            throws IOException, CodecException {
        try {
            return saveAsSac(seis, directory, null, null);
        } catch(NoPreferredOrigin e) {
            // cant happen as we are sending null
        }
        return null;
    }

    public static File saveAs(LocalSeismogramImpl seis,
                              File directory,
                              Channel channel,
                              CacheEvent event,
                              SeismogramFileTypes saveFileType)
            throws IOException, NoPreferredOrigin, CodecException,
            UnsupportedFileTypeException, SeedFormatException {
        if(saveFileType.equals(SeismogramFileTypes.SAC)) {
            return saveAsSac(seis, directory, channel, event);
        } else if(saveFileType.equals(SeismogramFileTypes.MSEED)) {
            return saveAsMSeed(seis, directory, channel, event);
        } else {
            throw new UnsupportedFileTypeException("Unsupported File Type "
                    + saveFileType.getName());
        }
    }

    public static File getUnusedFileName(File directory,
                                         Channel channel,
                                         String suffix) {
        File seisFile = getBaseFile(directory, channel, suffix);
        for(int n = 1; seisFile.exists(); n++) {
            seisFile = makeFile(directory, n, channel, suffix);
        }
        return seisFile;
    }

    public static File getBaseFile(File directory,
                                   Channel channel,
                                   String suffix) {
        return makeFile(directory, 0, channel, suffix);
    }

    public static File makeFile(File directory,
                                int count,
                                Channel channel,
                                String suffix) {
        String seisFilename = ChannelIdUtil.toStringNoDates(channel);
        seisFilename += count > 0 ? "" + count : "";
        seisFilename = seisFilename.replace(' ', '_');// make spacespace sites
        // '__'
        seisFilename += suffix;
        return new File(directory, seisFilename);
    }

    public static File saveAsMSeed(LocalSeismogramImpl seis,
                                   File directory,
                                   Channel channel,
                                   CacheEvent event)
            throws IOException, SeedFormatException {
        File seisFile = getUnusedFileName(directory, channel, ".mseed");
        return writeMSeed(seis, seisFile);
    }

    public static File writeMSeed(LocalSeismogramImpl seis, File seisFile)
            throws SeedFormatException, FileNotFoundException, IOException {
        DataRecord[] dr = FissuresConvert.toMSeed(seis);
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(seisFile)));
        for(int i = 0; i < dr.length; i++) {
            dr[i].write(dos);
        }
        dos.close();
        return seisFile;
    }

    public static File saveAsSac(LocalSeismogramImpl seis,
                                 File directory,
                                 Channel channel,
                                 CacheEvent event)
            throws IOException, NoPreferredOrigin, CodecException {
        File seisFile = getUnusedFileName(directory, channel, ".sac");
        SacTimeSeries sac = FissuresToSac.getSAC(seis,
                                                 channel,
                                                 event != null ? event.get_preferred_origin()
                                                         : null);
        sac.write(seisFile);
        return seisFile;
    }

}
