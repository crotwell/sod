package edu.sc.seis.sod.subsetter;

import org.w3c.dom.Element;

import edu.sc.seis.sod.ConfigurationException;

/**
 * @author groves Created on Mar 6, 2005
 */
public interface SubsetterLoader {

    public Subsetter load(Element el) throws ConfigurationException;
}