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

package magellan.library.tasks;

import magellan.library.HasRegion;

/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface Problem {
	/** DOCUMENT-ME */
	public static final int INFORMATION = 0;

	/** DOCUMENT-ME */
	public static final int WARNING = 1;

	/** DOCUMENT-ME */
	public static final int ERROR = 2;

	/**
	 * returns the creating inspector
	 *
	 * 
	 */
	public Inspector getInspector();

	/**
	 * returns the type of the problem
	 *
	 * 
	 */
	public int getType();

	/**
	 * returns the type of the problem
	 *
	 * 
	 */
	public int getLine();

	/**
	 * returns the object this problem criticizes
	 *
	 * 
	 */
	public HasRegion getObject();

	/**
	 * returns the originating object
	 *
	 * 
	 */
	public Object getSource();

	/**
	 * returns the message of the problem
	 *
	 * 
	 */
	public String toString();
}
