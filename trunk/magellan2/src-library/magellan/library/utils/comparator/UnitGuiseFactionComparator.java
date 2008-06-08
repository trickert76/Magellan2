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

import magellan.library.Faction;
import magellan.library.Unit;


/**
 * A comparator imposing an ordering on Unit objects by comparing the factions
 * they <em>pretend to</em> belong to.
 * 
 * <p>
 * Note: this comparator imposes orderings that are inconsistent with equals.
 * </p>
 * <p>
 * In order to overcome the inconsistency with equals this comparator allows the
 * introduction of a sub-comparator which is applied in cases of equality. I.e.
 * if the two compared units belong to the same faction and they would be
 * regarded as equal by this comparator, instead of 0 the result of the
 * sub-comparator's comparison is returned.
 * </p>
 */
public class UnitGuiseFactionComparator<E> implements Comparator<Unit> {
	protected Comparator<Faction> factionCmp = null;
	protected Comparator<E> sameFactionSubCmp = null;

	/**
	 * Creates a new UnitFactionComparator object.
	 *
	 * @param factionComparator the comparator used to compare the units' factions.
	 * @param sameFactionSubComparator if two units belonging to the same faction are compared,
	 * 		  this sub-comparator is applied if it is not <tt>null</tt>.
	 */
	public UnitGuiseFactionComparator(Comparator<Faction> factionComparator, Comparator<E> sameFactionSubComparator) {
		factionCmp = factionComparator;
		sameFactionSubCmp = sameFactionSubComparator;
	}

	/**
	 * Compares its two arguments for order according to the factions they are disguised as.
	 *
	 * @return the result of the faction comparator's comparison of <tt>o1</tt>'s and <tt>o2</tt>.
	 * 		   If are disguised as the same faction and a sub-comparator was specified, the result
	 * 		   that sub-comparator's comparison is returned.
	 */
	public int compare(Unit o1, Unit o2) {
		int retVal = 0;

		Faction f1 = o1.getGuiseFaction();
		Faction f2 = o2.getGuiseFaction();
		
//		if (f1==null){
//		  f1 = o1.getFaction();
//		}
//    if (f2==null){
//		  f2 = o2.getFaction();
//		}
		
		if(f1 == null) {
			if(f2 == null) {
				retVal = 0;
			} else {
				retVal = Integer.MAX_VALUE;
			}
		} else {
			if(f2 == null) {
				retVal = Integer.MIN_VALUE;
			} else {
				retVal = factionCmp.compare(f1, f2);

				if((retVal == 0) && (sameFactionSubCmp != null)) {
					retVal = sameFactionSubCmp.compare((E)o1, (E)o2);
				}
			}
		}

		return retVal;
	}

}
