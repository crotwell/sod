package edu.sc.seis.sod.subsetter.waveformArm;
import org.w3c.dom.Element;
import edu.sc.seis.sod.ConfigurationException;



public class PhaseWindow {
	
	public PhaseWindow(Element config) throws ConfigurationException {
		phaseRequest = new PhaseRequest(config);
	}
	public PhaseRequest getPhaseRequest() {
		return phaseRequest;
	}
	
	private PhaseRequest phaseRequest;
}
