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

package magellan.library.io;

import java.io.IOException;

import magellan.library.io.cr.CRGameNameIO;
import magellan.library.io.file.FileType;
import magellan.library.io.xml.XMLGameNameIO;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 305 $
 */
public class GameNameReader {

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static String getGameName(FileType filetype) {
		try {
			String gameName = new CRGameNameIO().getGameName(filetype);

			return (gameName != null) ? gameName : new XMLGameNameIO().getGameName(filetype);
		} catch(IOException e) {
			return null;
		}
	}
}
