package edu.sc.seis.sod.subsetter.station;

import java.time.Duration;
import java.time.Instant;

import org.w3c.dom.Element;

import edu.sc.seis.seisFile.fdsnws.FDSNWSException;
import edu.sc.seis.seisFile.fdsnws.IRISWSVirtualNetworkQuerier;
import edu.sc.seis.seisFile.fdsnws.IRISWSVirtualNetworkQueryParams;
import edu.sc.seis.seisFile.fdsnws.stationxml.Station;
import edu.sc.seis.seisFile.fdsnws.virtualnet.ContributorNetwork;
import edu.sc.seis.seisFile.fdsnws.virtualnet.VirtualNetwork;
import edu.sc.seis.seisFile.fdsnws.virtualnet.VirtualNetworkList;
import edu.sc.seis.seisFile.fdsnws.virtualnet.VirtualStation;
import edu.sc.seis.sod.ConfigurationException;
import edu.sc.seis.sod.SodUtil;
import edu.sc.seis.sod.Start;
import edu.sc.seis.sod.model.common.MicroSecondDate;
import edu.sc.seis.sod.model.common.TimeInterval;
import edu.sc.seis.sod.model.station.NetworkIdUtil;
import edu.sc.seis.sod.source.SodSourceException;
import edu.sc.seis.sod.source.network.NetworkSource;
import edu.sc.seis.sod.status.Fail;
import edu.sc.seis.sod.status.Pass;
import edu.sc.seis.sod.status.StringTree;
import edu.sc.seis.sod.util.time.ClockUtil;

public class BelongsToVirtual implements StationSubsetter {

    private VirtualNetworkList getVirtual(String host, String code) throws ConfigurationException, FDSNWSException {
        IRISWSVirtualNetworkQueryParams qp = new IRISWSVirtualNetworkQueryParams(host);
        qp.setCode(code);
        IRISWSVirtualNetworkQuerier querier = new IRISWSVirtualNetworkQuerier(qp);
        VirtualNetworkList vnet = querier.getVirtual();
        return vnet;
    }

    public BelongsToVirtual(Element el) throws ConfigurationException {
        this(SodUtil.getNestedText(el), null);
    }

    public BelongsToVirtual(String virtualNetCode, Duration refreshInterval) {
        this.code = virtualNetCode;
        this.refreshInterval = refreshInterval;
    }

    public StringTree accept(Station station, NetworkSource network)
            throws ConfigurationException, SodSourceException {
        try {
            refreshStations(network);
            for (VirtualNetwork vnet : vnetList.getVirtualNetworks()) {
                for (ContributorNetwork cn : vnet.getContribNetList()) {
                    if (station.getNetwork().getCode().equals(cn.getCode())
                            && (!NetworkIdUtil.isTemporary(station.getNetwork())
                                    || cn.getStartYear().equals(NetworkIdUtil.getYear(station.getNetwork())))) {
                        for (VirtualStation vsta : cn.getStationList()) {
                            if (station.getStationCode().equals(vsta.getCode())) {
                                return new Pass(this);
                            }
                        }
                    }
                }
            }
            return new Fail(this);
        } catch(FDSNWSException e) {
            throw new SodSourceException("Problem getting virtual networks", e);
        }
    }

    private void refreshStations(NetworkSource network)
            throws ConfigurationException, SodSourceException, FDSNWSException {
        if (ClockUtil.now().minus(getRefreshInterval()).isAfter(lastQuery)) {
            lastQuery = ClockUtil.now();
            vnetList = getVirtual(host, code);
        }
    }

    public Duration getRefreshInterval() {
        if (refreshInterval == null) {
            refreshInterval = Start.getNetworkArm().getRefreshInterval();
        }
        return refreshInterval;
    }

    private String host = "http://service.iris.edu";

    private String path = "irisws/virtualnetwork/1/query";

    private String code;

    private VirtualNetworkList vnetList;

    private Duration refreshInterval;

    private Instant lastQuery =  ClockUtil.wayPast();
}
