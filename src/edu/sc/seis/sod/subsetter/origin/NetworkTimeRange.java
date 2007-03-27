package edu.sc.seis.sod.subsetter.origin;

import edu.iris.Fissures.IfEvent.EventAccessOperations;
import edu.iris.Fissures.IfEvent.EventAttr;
import edu.iris.Fissures.IfEvent.Origin;
import edu.iris.Fissures.model.MicroSecondDate;
import edu.sc.seis.fissuresUtil.display.MicroSecondTimeRange;
import edu.sc.seis.sod.Arm;
import edu.sc.seis.sod.ArmListener;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.NetworkArm;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.UserConfigurationException;
import edu.sc.seis.sod.database.ChannelDbObject;
import edu.sc.seis.sod.database.NetworkDbObject;
import edu.sc.seis.sod.database.SiteDbObject;
import edu.sc.seis.sod.database.StationDbObject;
import edu.sc.seis.sod.source.event.MicroSecondTimeRangeSupplier;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.status.StringTreeLeaf;

public class NetworkTimeRange implements OriginSubsetter, ArmListener,
		MicroSecondTimeRangeSupplier {

	public NetworkTimeRange() throws ConfigurationException {
		Start.add(this);
		// Don't get the network time range from the event arm!
		Start.getRunProps().setAllowDeadNets(true);
	}

	public void starting(Arm arm) throws ConfigurationException {
		if (!(arm instanceof NetworkArm)) {
			return;
		}
		this.arm = (NetworkArm) arm;
		this.arm.add(this);
	}

	public void started() throws ConfigurationException {
		if (arm == null) {
			throw new UserConfigurationException(
					"Using networkTimeRange in the event arm requires a network arm");
		}
	}

	public void finished(Arm arm) {
		synchronized (finishLock) {
			finishLock.notify();
		}
	}

	public StringTree accept(EventAccessOperations event, EventAttr eventAttr,
			Origin origin) {
		return new StringTreeLeaf(this, getMSTR().contains(
				new MicroSecondDate(origin.origin_time)));
	}

	public synchronized MicroSecondTimeRange getMSTR() {
		if (range != null) {
			return range;
		}
		synchronized (finishLock) {
			try {
				finishLock.wait();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		NetworkDbObject[] nets;
		try {
			nets = arm.getSuccessfulNetworks();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		for (int i = 0; i < nets.length; i++) {
			StationDbObject[] stas = arm.getSuccessfulStations(nets[i]);
			for (int j = 0; j < stas.length; j++) {
				SiteDbObject[] sites = arm.getSuccessfulSites(nets[i], stas[j]);

				for (int k = 0; k < sites.length; k++) {
					ChannelDbObject[] chans = arm.getSuccessfulChannels(
							nets[i], sites[k]);
					for (int l = 0; l < chans.length; l++) {
						MicroSecondTimeRange chanRange = new MicroSecondTimeRange(
								chans[l].getChannel().effective_time);
						if (range == null) {
							range = chanRange;
						} else {
							if (chanRange.getBeginTime().before(
									range.getBeginTime())) {
								range = new MicroSecondTimeRange(chanRange
										.getBeginTime(), range.getEndTime());
							}
							if (chanRange.getEndTime().after(range.getEndTime())) {
								range = new MicroSecondTimeRange(range
										.getBeginTime(), chanRange.getEndTime());
							}
						}
						
					}
				}
			}
		}
		if(range == null){
			range = new MicroSecondTimeRange(new MicroSecondDate(), new MicroSecondDate());
		}
		System.out.println("GOT " + range);
		return range;

	}

	private NetworkArm arm;

	private Object finishLock = new Object();

	private MicroSecondTimeRange range;
}
