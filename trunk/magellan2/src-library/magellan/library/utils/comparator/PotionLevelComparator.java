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

package magellan.library.utils.comparator;

import java.util.Comparator;

import magellan.library.Potion;


/**
 * A comparator imposing an ordering on Potion objects by comparing their levels.
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * 
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the introduction of a
 * sub-comparator which is applied in cases of equality. I.e. if the two compared potions have the
 * same level and they would be regarded as equal by this comparator, instead of 0 the result of
 * the sub-comparator's comparison is returned.
 * </p>
 */
public class PotionLevelComparator implements Comparator<Potion> {
	private Comparator<? super Potion> sameLevelSubCmp = null;

	/**
	 * Creates a new SpellLevelComparator object.
	 *
	 * @param sameLevelSubComparator if two spells with the same level are compared, this
	 * 		  sub-comparator is applied if it is not <tt>null</tt>.
	 */
	public PotionLevelComparator(Comparator<? super Potion> sameLevelSubComparator) {
		sameLevelSubCmp = sameLevelSubComparator;
	}

	/**
	 * Compares its two arguments for order according to their levels
	 *
	 * 
	 * 
	 *
	 * @return the difference of <tt>o1</tt>'s and <tt>o2</tt>'s numerical level value. If they are
	 * 		   equal and a sub-comparator was specified, the result that sub-comparator's
	 * 		   comparison is returned.
	 */
	public int compare(Potion o1, Potion o2) {
		int l1 = o1.getLevel();
		int l2 = o2.getLevel();

		if((l1 == l2) && (sameLevelSubCmp != null)) {
			return sameLevelSubCmp.compare(o1, o2);
		}

		return l1 - l2;
	}
}
