package edu.sc.seis.sod;

/**
 * Notifies the given object when the arms of SOD are starting and, if it calls
 * add on the arm when it gets in starting, when the arm is finished.
 * 
 */
public interface ArmListener {
	public void finished(Arm arm);

	public void starting(Arm arm) throws ConfigurationException;

	/**
	 * Called when all the arms have been started.
	 */
	public void started() throws ConfigurationException;
}
