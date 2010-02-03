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

package magellan.library.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import magellan.library.CoordinateID;
import magellan.library.gamebinding.EresseaConstants;


/**
 * A class providing convience functions for handling directions like in ships or borders. There
 * are three direction formats and the coversions between them supported: integer representation
 * (0 = north west and clockwise up), string representation (like 'NW' or 'Nordwesten') and
 * relative coordinate representation (coordinate with x = -1, y = 1).
 */
public class Direction {
	/** Invalid/unknown direction */
	public static final int DIR_INVALID = -1;

	/** north west direction */
	public static final int DIR_NW = 0;

	/** north east direction */
	public static final int DIR_NE = 1;

	/** east direction */
	public static final int DIR_E = 2;

	/** south east direction */
	public static final int DIR_SE = 3;

	/** south west direction */
	public static final int DIR_SW = 4;

	/** west direction */
	public static final int DIR_W = 5;
	private static List<String> shortNames = null;
	private static List<String> longNames = null;
  private static List<String> normalizedLongNames = null;
  
	private static Locale usedLocale = null;

	private int dir = Direction.DIR_INVALID;

	/**
	 * Creates a new Direction object interpreting the specified integer as a direction according
	 * to the direction constants of this class.
	 *
	 * 
	 */
	public Direction(int direction) {
		if((direction > -1) && (direction < 6)) {
			dir = direction;
		} else {
			dir = Direction.DIR_INVALID;
		}
	}

	/**
	 * Creates a new Direction object interpreting the specified coordinate as a direction.
	 *
	 * @param c a relative coordinate, e.g. (1, 0) for DIR_E. If c is null an
	 * 		  IllegalArgumentException is thrown.
	 *
	 * @throws IllegalArgumentException if the c param is null.
	 */
	public Direction(CoordinateID c) {
		if(c != null) {
			dir = Direction.toInt(c);
		} else {
			throw new IllegalArgumentException("Direction.Direction(Coordinate c): invalid coordinate specified!");
		}
	}

	/**
	 * Creates a new Direction object interpreting the specified String as a direction.
	 *
	 * @param str a german name for a direction, e.g. "Osten" for DIR_E. If str is null an
	 * 		  IllegalArgumentException is thrown.
	 *
	 * @throws IllegalArgumentException if the str param is null.
	 */
	public Direction(String str) {
		if(str != null) {
			dir = Direction.toInt(str);
		} else {
			throw new IllegalArgumentException("Direction.Direction(String str): invalid string specified!");
		}
	}

	/**
	 * Returns the actual direction of this object.
	 *
	 * 
	 */
	public int getDir() {
		return dir;
	}

	/**
	 * Returns a String representation for this direction.
	 *
	 * 
	 */
	@Override
  public String toString() {
		return Direction.toString(this.dir, false);
	}

	/**
	 * Returns a relative coordinate representing the direction dir. E.g. the direction DIR_W would
	 * create the coordinate (-1, 0).
	 *
	 * 
	 *
	 * 
	 */
	public static CoordinateID toCoordinate(int dir) {
		int x = 0;
		int y = 0;

		switch(dir) {
		case DIR_NW:
			x = -1;
			y = 1;
			break;
		case DIR_NE:
			y = 1;
			break;
		case DIR_SE:
			y = -1;
			x = 1;
			break;
		case DIR_E:
			x = 1;
			break;
		case DIR_SW:
			y = -1;
			break;
		case DIR_W:
			x = -1;
			break;
		}

		return new CoordinateID(x, y);
	}

	/**
	 * Returns a String representation of the specified direction.
	 *
	 * 
	 *
	 * 
	 */
	public static String toString(int dir) {
		return Direction.toString(dir, false);
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 *
	 * 
	 */
	public static String toString(CoordinateID c) {
		return Direction.toString(Direction.toInt(c));
	}

	/**
	 * Returns a String representation of the specified direction.
	 *
	 * @param dir if true, a short form of the direction's string representation is returned.
	 * 
	 *
	 * 
	 */
	public static String toString(int dir, boolean shortForm) {
		if((dir < Direction.DIR_NW) || (dir > Direction.DIR_W)) {
			dir = Direction.DIR_INVALID;
		}

		if(shortForm) {
			return Direction.getShortDirectionString(dir);
		} else {
			return Direction.getLongDirectionString(dir);
		}
	}

	/**
	 * Converts a relative coordinate to an integer representation of the direction.
	 *
	 * 
	 *
	 * 
	 */
	public static int toInt(CoordinateID c) {
		int dir = Direction.DIR_INVALID;

		if(c.x == -1) {
			if(c.y == 0) {
				dir = Direction.DIR_W;
			} else if(c.y == 1) {
				dir = Direction.DIR_NW;
			}
		} else if(c.x == 0) {
			if(c.y == -1) {
				dir = Direction.DIR_SW;
			} else if(c.y == 1) {
				dir = Direction.DIR_NE;
			}
		} else if(c.x == 1) {
			if(c.y == -1) {
				dir = Direction.DIR_SE;
			} else if(c.y == 0) {
				dir = Direction.DIR_E;
			}
		}

		return dir;
	}

	/**
	 * Converts a string to an integer representation of the direction.
	 *
	 * 
	 *
	 * 
	 */
	public static int toInt(String str) {
		int dir = Direction.DIR_INVALID;
		String s = Umlaut.normalize(str).toLowerCase();

		dir = Direction.find(s, Direction.getShortNames());

		if(dir == Direction.DIR_INVALID) {
			dir = Direction.find(s, Direction.getNormalizedLongNames());
		}

		return dir;
	}

  private static String getLongDirectionString(int key) {
		switch(key) {
		case DIR_NW:
			return Resources.getOrderTranslation(EresseaConstants.O_NORTHWEST);

		case DIR_NE:
			return Resources.getOrderTranslation(EresseaConstants.O_NORTHEAST);

		case DIR_E:
			return Resources.getOrderTranslation(EresseaConstants.O_EAST);

		case DIR_SE:
			return Resources.getOrderTranslation(EresseaConstants.O_SOUTHEAST);

		case DIR_SW:
			return Resources.getOrderTranslation(EresseaConstants.O_SOUTHWEST);

		case DIR_W:
			return Resources.getOrderTranslation(EresseaConstants.O_WEST);
		}

		return Resources.get("util.direction.name.long.invalid");
	}

	private static String getShortDirectionString(int key) {
		switch(key) {
		case DIR_NW:
			return Resources.getOrderTranslation(EresseaConstants.O_NW);

		case DIR_NE:
			return Resources.getOrderTranslation(EresseaConstants.O_NE);

		case DIR_E:
			return Resources.getOrderTranslation(EresseaConstants.O_E);

		case DIR_SE:
			return Resources.getOrderTranslation(EresseaConstants.O_SE);

		case DIR_SW:
			return Resources.getOrderTranslation(EresseaConstants.O_SW);

		case DIR_W:
			return Resources.getOrderTranslation(EresseaConstants.O_W);
		}

		return Resources.get("util.direction.name.short.invalid");
	}

	/**
	 * Returns the names of all valid directions in an all-lowercase short form.
	 *
	 * 
	 */
	public static List<String> getShortNames() {
		if(!Locales.getOrderLocale().equals(Direction.usedLocale)) {
			Direction.shortNames = null;
			Direction.longNames = null;
      Direction.normalizedLongNames = null;
		}

		if(Direction.shortNames == null) {
			Direction.usedLocale = Locales.getOrderLocale();
			Direction.shortNames = new ArrayList<String>(6);

			for(int i = 0; i < 6; i++) {
				Direction.shortNames.add(Direction.getShortDirectionString(i).toLowerCase());
			}
		}

		return Direction.shortNames;
	}

	/**
	 * Returns the names of all valid directions in an all-lowercase long form.
	 *
	 * 
	 */
	public static List<String> getLongNames() {
		if(!Locales.getOrderLocale().equals(Direction.usedLocale)) {
			Direction.shortNames = null;
			Direction.longNames = null;
			Direction.normalizedLongNames = null;
		}

		if(Direction.longNames == null) {
			Direction.usedLocale = Locales.getOrderLocale();
			Direction.longNames = new ArrayList<String>(6);

			for(int i = 0; i < 6; i++) {
				Direction.longNames.add(Direction.getLongDirectionString(i).toLowerCase());
			}
		}

		return Direction.longNames;
	}

	private static List<String> getNormalizedLongNames() {
	  if (!Locales.getOrderLocale().equals(Direction.usedLocale)) {
      Direction.shortNames = null;
      Direction.longNames = null;
      Direction.normalizedLongNames = null;
    }

    if (Direction.normalizedLongNames == null) {
      Direction.usedLocale = Locales.getOrderLocale();
      Direction.normalizedLongNames = new ArrayList<String>(6);

      for (int i = 0; i < 6; i++) {
        Direction.normalizedLongNames.add(Umlaut
            .convertUmlauts(Direction.getLongDirectionString(i)).toLowerCase());
      }
    }

    return Direction.normalizedLongNames;
  }


	/**
	 * Finds pattern in the set of matches (case-sensitively) and returns the index of the hit.
	 * Pattern may be an abbreviation of any of the matches. If pattern is ambiguous or cannot be
	 * found among the matches, -1 is returned
	 *
	 * 
	 * 
	 *
	 * 
	 */
	private static int find(String pattern, List<String> matches) {
		int i = 0;
		int hitIndex = -1;
		int hits = 0;

		for(Iterator<String> iter = matches.iterator(); iter.hasNext(); i++) {
			String match = iter.next();

			if(match.startsWith(pattern)) {
				hits++;
				hitIndex = i;
			}
		}

		if(hits == 1) {
			return hitIndex;
		} else {
			return -1;
		}
	}
}
