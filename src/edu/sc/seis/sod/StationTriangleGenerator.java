package edu.sc.seis.sod;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.varia.NullAppender;
import edu.sc.seis.fissuresUtil.display.DisplayUtils;
import edu.sc.seis.sod.database.event.JDBCEventStatus;
import edu.sc.seis.sod.status.waveformArm.MapWaveformStatus;

/**
 * @author groves Created on Aug 24, 2004
 */
public class StationTriangleGenerator {

	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure(new NullAppender());
		MapWaveformStatus map = new MapWaveformStatus();
		JDBCEventStatus events = new JDBCEventStatus();
		map.add(events.getAll()[0], "stationMap.png");
		map.run();
		int[][][] stationLocs = null;//map.stationLocs;
        for (int i = 0; i < stationLocs.length; i++) {
            BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_4BYTE_ABGR);
            Graphics2D g = img.createGraphics();
            g.setColor(new Color(0, 0, 0, 0));
            g.fillRect(0, 0, 640, 480);
            g.setColor(Color.WHITE);
            Polygon pg = new Polygon();
            for (int j = 0; j < stationLocs[i][0].length; j++) {
                pg.addPoint(stationLocs[i][0][j], stationLocs[i][1][j]);
			}
            g.setStroke(DisplayUtils.TWO_PIXEL_STROKE);
            g.draw(pg);
            ImageIO.write(img, "png", new File(i + ".png"));
            System.out.println("<img src=\""+i+".png\" id=\""+i+"\" usemap=\"map\" class=\"hide\"/>");
		}
	}
}