/*
 *  Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe,
 *                          Stefan Goetz, Sebastian Pappert,
 *                          Klaas Prause, Enno Rehling,
 *                          Sebastian Tusk, Ulrich Kuester,
 *                          Ilja Pavkovic
 *
 * This file is part of the Eressea Java Code Base, see the
 * file LICENSING for the licensing information applying to
 * this file.
 *
 */

package magellan.client.swing.map;

/**
 * Simple interface to get connected to a mapper. Should be used by renderers which want to set
 * some Mapper properties.
 *
 * @author Andreas
 * @version 1.0
 */
public interface MapperAware {
	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void setMapper(Mapper mapper);
}
