package edu.sc.seis.sod.process.waveform.vector;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import de.erichseifert.gral.data.AbstractDataSource;
import de.erichseifert.gral.data.DataSource;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.data.statistics.Statistics;
import de.erichseifert.gral.io.plots.DrawableWriter;
import de.erichseifert.gral.io.plots.DrawableWriterFactory;
import de.erichseifert.gral.plots.Plot;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.axes.Axis;
import de.erichseifert.gral.plots.axes.AxisRenderer;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.util.Insets2D;
import edu.iris.Fissures.FissuresException;
import edu.iris.Fissures.Location;
import edu.iris.Fissures.Orientation;
import edu.iris.Fissures.IfSeismogramDC.RequestFilter;
import edu.iris.Fissures.network.ChannelIdUtil;
import edu.iris.Fissures.network.ChannelImpl;
import edu.iris.Fissures.seismogramDC.LocalSeismogramImpl;
import edu.sc.seis.fissuresUtil.bag.Rotate;
import edu.sc.seis.fissuresUtil.cache.CacheEvent;
import edu.sc.seis.fissuresUtil.cache.EventUtil;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.fissuresUtil.display.SeismogramPDFBuilder;
import edu.sc.seis.fissuresUtil.display.configuration.BorderConfiguration;
import edu.sc.seis.fissuresUtil.display.configuration.DOMHelper;
import edu.sc.seis.fissuresUtil.hibernate.ChannelGroup;
import edu.sc.seis.sod.CookieJar;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.process.waveform.AbstractFileWriter;
import edu.sc.seis.sod.process.waveform.PhaseCut;
import edu.sc.seis.sod.process.waveform.PhaseWindow;
import edu.sc.seis.sod.process.waveform.SeismogramTitler;
import edu.sc.seis.sod.status.StringTreeBranch;
import edu.sc.seis.sod.status.StringTreeLeaf;
import edu.sc.seis.sod.velocity.network.VelocityChannel;

public class ParticleMotionPlot extends AbstractFileWriter implements WaveformVectorProcess {

    public ParticleMotionPlot(Element config) throws Exception {
        this(extractWorkingDir(config),
             extractFileTemplate(config, DEFAULT_FILE_TEMPLATE),
             extractPrefix(config));

        if(DOMHelper.hasElement(config, "titleBorder")) {
            titleBorder = new BorderConfiguration();
            titleBorder.configure(DOMHelper.getElement(config, "titleBorder"));
            titler = new SeismogramTitler(titleBorder);
        }

        if(DOMHelper.hasElement(config, "phaseWindow")) {
            phaseWindow = new PhaseWindow(SodUtil.getElement(config, "phaseWindow"));
            cutter = new PhaseCut(phaseWindow.getPhaseRequest());
        }
    }

    public ParticleMotionPlot(String workingDir, String fileTemplate, String prefix) throws Exception {
        super(workingDir, fileTemplate, prefix);
    }

    public WaveformVectorResult accept(CacheEvent event,
                                       ChannelGroup channelGroup,
                                       RequestFilter[][] original,
                                       RequestFilter[][] available,
                                       LocalSeismogramImpl[][] seismograms,
                                       CookieJar cookieJar) throws Exception {
        WaveformVectorResult cutResult = new ANDWaveformProcessWrapper(cutter).accept(event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         seismograms,
                                                         cookieJar);
        if (!cutResult.isSuccess()) {
            return new WaveformVectorResult(false,
                                            cutResult.getSeismograms(),
                                            new StringTreeBranch(this, false, cutResult.getReason()));
        }
        WaveformVectorResult trimResult = trimmer.accept(event,
                                                         channelGroup,
                                                         original,
                                                         available,
                                                         cutResult.getSeismograms(),
                                                         cookieJar);
        if (!trimResult.isSuccess()) {
            return new WaveformVectorResult(false,
                                            trimResult.getSeismograms(),
                                            new StringTreeBranch(this, false, trimResult.getReason()));
        }
        LocalSeismogramImpl[][] cutAndTrim = trimResult.getSeismograms();
            
        // find x & y channel, y should be x+90 degrees and horizontal
        ChannelImpl[] horizontal = channelGroup.getHorizontalXY();
        if (horizontal.length == 0) {
            Orientation o1 = channelGroup.getChannel1().getOrientation();
            Orientation o2 = channelGroup.getChannel2().getOrientation();
            Orientation o3 = channelGroup.getChannel3().getOrientation();
            return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                            false,
                                                                            "Channels not rotatable, unable to find horizontals with 90 deg separation: "
                                                                                    + o1.azimuth + "/" + o1.dip + " "
                                                                                    + o2.azimuth + "/" + o2.dip + " "
                                                                                    + o3.azimuth + "/" + o3.dip + " "));
        }
        int xIndex = -1, yIndex = -1;
        for (int i = 0; i < cutAndTrim.length; i++) {
            if (cutAndTrim[i].length != 0) {
                if (ChannelIdUtil.areEqual(seismograms[i][0].channel_id, horizontal[0].get_id())) {
                    xIndex = i;
                }
                if (ChannelIdUtil.areEqual(seismograms[i][0].channel_id, horizontal[1].get_id())) {
                    yIndex = i;
                }
            }
        }
        int zIndex = -1;
        for (int i = 0; i < 3; i++) {
            if (i != xIndex && i != yIndex) {
                zIndex = i;
                break;
            }
        }
        if (xIndex == -1 || yIndex == -1) {
            return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                            false,
                                                                            "Can't find seismograms to match horizontal channels: xIndex="
                                                                                    + xIndex + " yIndex=" + yIndex));
        }
        if (cutAndTrim[xIndex].length != cutAndTrim[yIndex].length) {
            return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                            false,
                                                                            "Seismogram lengths for horizontal channels don't match: "
                                                                                    + seismograms[xIndex].length
                                                                                    + " != "
                                                                                    + seismograms[yIndex].length));
        }
        if (doVerticalPlots && cutAndTrim[xIndex].length != cutAndTrim[zIndex].length) {
            return new WaveformVectorResult(seismograms, new StringTreeLeaf(this,
                                                                            false,
                                                                            "Seismogram lengths for horizontal and vertical channels don't match: "
                                                                                    + seismograms[xIndex].length
                                                                                    + " != "
                                                                                    + seismograms[zIndex].length));
        }
        for (int i = 0; i < cutAndTrim[xIndex].length; i++) {
            if (cutAndTrim[xIndex][i].getNumPoints() != cutAndTrim[yIndex][i].getNumPoints()) {
                return new WaveformVectorResult(seismograms, new StringTreeLeaf(this, false, i
                        + " Seismogram num points for horizontal channels don't match: "
                        + cutAndTrim[xIndex][i].getNumPoints() + " != " + cutAndTrim[yIndex][i].getNumPoints()));
            }
            if (doVerticalPlots && cutAndTrim[xIndex][i].getNumPoints() != cutAndTrim[zIndex][i].getNumPoints()) {
                return new WaveformVectorResult(seismograms, new StringTreeLeaf(this, false, i
                        + " Seismogram num points for horizontal channels don't match: "
                        + cutAndTrim[xIndex][i].getNumPoints() + " != " + cutAndTrim[zIndex][i].getNumPoints()));
            }
        }
        Map<String, Object> extras = new HashMap<String, Object>();
        Plot plot = makePlot(cutAndTrim[xIndex], horizontal[0], cutAndTrim[yIndex], horizontal[1], event);
        savePlot(plot, event, horizontal[0], horizontal[1], extras);
        if (doVerticalPlots) {
            plot = makePlot(cutAndTrim[xIndex], horizontal[0], cutAndTrim[zIndex], channelGroup.getVertical(), event);
            savePlot(plot, event, horizontal[0], channelGroup.getVertical(), extras);
            plot = makePlot(cutAndTrim[yIndex], horizontal[1], cutAndTrim[zIndex], channelGroup.getVertical(), event);
            savePlot(plot, event, horizontal[1], channelGroup.getVertical(), extras);
        }
        return new WaveformVectorResult(seismograms, new StringTreeLeaf(this, true));
    }

    public Plot makePlot(LocalSeismogramImpl[] XSeis,
                                ChannelImpl xChan,
                                LocalSeismogramImpl[] ySeis,
                                ChannelImpl yChan,
                                CacheEvent event) throws Exception {

        MicroSecondTimeRange timeWindow = null;
        timeWindow = new MicroSecondTimeRange(phaseWindow.getPhaseRequest().generateRequest(event, xChan));
        if(titler != null) {
            titler.title(event, xChan, timeWindow);
        }
        DataSource data;
        if (yChan.getOrientation().dip > -5) {
            // horizontal (dip 0)
            data = new SeisPlotDataSource(XSeis[0],
                                          xChan.getOrientation().azimuth,
                                          ySeis[0],
                                          yChan.getOrientation().azimuth);

        } else {
            // vertical, mul -1 to make dip look like azimuth
            data = new SeisPlotDataSource(XSeis[0],
                                          -1*xChan.getOrientation().dip,
                                          ySeis[0],
                                          -1*yChan.getOrientation().dip);
        }
        XYPlot plot = new XYPlot(data);
        plot.setInsets(new Insets2D.Double(20, 50, 50, 20));
        LineRenderer lr1 = new DefaultLineRenderer2D();
        lr1.setSetting(LineRenderer.COLOR, Color.RED);
        plot.setLineRenderer(data, lr1);
        double insetsTop = 20.0, insetsLeft = 60.0, insetsBottom = 60.0, insetsRight = 40.0;
        plot.setInsets(new Insets2D.Double(insetsTop, insetsLeft, insetsBottom, insetsRight));
        plot.setSetting(Plot.TITLE,
                        titleBorder.getTitles()[0].getTitle());
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.LABEL,
                                                       ChannelIdUtil.toStringNoDates(xChan) + " "
                                                               + xChan.getOrientation().azimuth);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.LABEL,
                                                       ChannelIdUtil.toStringNoDates(yChan) + " "
                                                               + yChan.getOrientation().azimuth);
        Statistics dataStats = data.getStatistics();
        dataStats.get(Statistics.MIN);
        Collection<String> axisList = plot.getAxesNames();
        float range = 0;
        for (String axisName : axisList) {
            Axis axis = plot.getAxis(axisName);
            if (axis.getRange() > range) {
                range = (float)axis.getRange();
            }
        }
        for (String axisName : axisList) {
            Axis axis = plot.getAxis(axisName);
            float middle = (axis.getMin().floatValue() + axis.getMax().floatValue()) / 2;
            axis.setRange(middle - range / 2, middle + range / 2);
        }
        plot.getAxisRenderer(XYPlot.AXIS_X).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        plot.getAxisRenderer(XYPlot.AXIS_Y).setSetting(AxisRenderer.INTERSECTION, -Double.MAX_VALUE);
        if (xChan.getOrientation().dip == 0 && yChan.getOrientation().dip == 0) {
            Location staLoc = xChan.getSite().getLocation();
            Location eventLoc = EventUtil.extractOrigin(event).getLocation();
            float baz = (float)(180 + Rotate.getRadialAzimuth(staLoc, eventLoc)) % 360;
            DataTable bazLine = new DataTable(Float.class, Float.class);
            float xMid = (plot.getAxis(XYPlot.AXIS_X).getMin().floatValue() + plot.getAxis(XYPlot.AXIS_X)
                    .getMax()
                    .floatValue()) / 2;
            float yMid = (plot.getAxis(XYPlot.AXIS_Y).getMin().floatValue() + plot.getAxis(XYPlot.AXIS_Y)
                    .getMax()
                    .floatValue()) / 2;
            float x = (float)(range / 2 * Math.cos(Math.toRadians(90 - baz)));
            float y = (float)(range / 2 * Math.sin(Math.toRadians(90 - baz)));
            bazLine.add(xMid - x, yMid - y);
            bazLine.add(xMid + x, yMid + y);
            plot.add(bazLine);
            LineRenderer bazlr = new DefaultLineRenderer2D();
            bazlr.setSetting(LineRenderer.COLOR, Color.GREEN);
            plot.setLineRenderer(bazLine, bazlr);
        }
        return plot;
    }
    
    public void savePlot(Plot plot, 
                         CacheEvent event,
                         ChannelImpl channel,
                         ChannelImpl otherChannel,
                         Map<String, Object> extras) throws IOException {
        removeExisting(event, channel, otherChannel, extras);

        String picFileName = generate(event, channel, otherChannel, 0, extras);
        File f = new File(picFileName);
        File parent = f.getParentFile();
        if(!parent.exists() && !parent.mkdirs()) {
            throw new IOException("Unable to create directory "+ parent);
        }
        
        DrawableWriter writer = DrawableWriterFactory.getInstance().get("application/pdf");
        writer.write(plot, new FileOutputStream(f), 800, 800);
    }
    
    boolean doVerticalPlots = true;

    BorderConfiguration titleBorder;
    
    SeismogramTitler titler;
    
    PhaseWindow phaseWindow;
    
    public static String DEFAULT_FILE_TEMPLATE = "Event_${event.getTime('yyyy_MM_dd_HH_mm_ss')}/${prefix}${station}_${chan.get_code()}_${otherChannel.get_code()}_${index}.pdf";
    
    SeismogramPDFBuilder pdfBuilder = new SeismogramPDFBuilder();

    private VectorTrim trimmer = new VectorTrim();
    
    private PhaseCut cutter;

    private static Logger logger = LoggerFactory.getLogger(ParticleMotionPlot.class);

}


class SeisPlotDataSource extends AbstractDataSource {

    SeisPlotDataSource(LocalSeismogramImpl seisX, float xAz, LocalSeismogramImpl seisY, float yAz) {
        super(Float.class, Float.class);
        assert seisX.getNumPoints() == seisY.getNumPoints();
        this.seisX = seisX;
        this.seisY = seisY;
        this.xAz = xAz;
        this.yAz = yAz;
    }

    LocalSeismogramImpl seisX;

    LocalSeismogramImpl seisY;

    float xAz, yAz;

    MicroSecondTimeRange timeWindow;
    
    public Number get(int col, int row) {
        try {
            double xRad = Math.toRadians(90 - xAz);
            double yRad = Math.toRadians(90 - yAz);
            if (col == 0) {
                return (float)(Math.cos(xRad) * seisX.get_as_floats()[row] + Math.cos(yRad)
                        * seisY.get_as_floats()[row]);
            }
            return (float)(Math.sin(xRad) * seisX.get_as_floats()[row] + Math.sin(yRad)
                    * seisY.get_as_floats()[row]);
        } catch(FissuresException e) {
            throw new RuntimeException("Should not happen, but I guess it did", e);
        }
    }

    public int getRowCount() {
        return seisX.getNumPoints();
    }
}