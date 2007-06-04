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

package magellan.library.gamebinding;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import magellan.library.Building;
import magellan.library.CoordinateID;
import magellan.library.EntityID;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.ID;
import magellan.library.IntegerID;
import magellan.library.Message;
import magellan.library.Region;
import magellan.library.Ship;
import magellan.library.Skill;
import magellan.library.StringID;
import magellan.library.Unique;
import magellan.library.Unit;
import magellan.library.rules.BuildingType;
import magellan.library.rules.ItemType;
import magellan.library.rules.RegionType;
import magellan.library.utils.Regions;
import magellan.library.utils.Sorted;
import magellan.library.utils.comparator.IDComparator;
import magellan.library.utils.comparator.SortIndexComparator;


/**
 * DOCUMENT-ME
 *
 * @author $Author: $
 * @version $Revision: 345 $
 */
public class EresseaPostProcessor {
	private EresseaPostProcessor() {
	}

	private static final EresseaPostProcessor singleton = new EresseaPostProcessor();

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public static EresseaPostProcessor getSingleton() {
		return singleton;
	}

	/**
	 * This method tries to fix some issues that arise right after reading a report file.
	 *
	 * It scans messages for herbs, removes dummy units, creates temp units and tries to 
	 * detect if resources should be set to zero because they are not in the report.
	 *
	 * @param data 
	 */
	public void postProcess(GameData data) {
		/* scan the messages for additional information */
		if((data != null) && (data.factions() != null)) {
			for(Iterator factions = data.factions().values().iterator(); factions.hasNext();) {
				Faction f = (Faction) factions.next();

				if(f.getMessages() != null) {
					for(Iterator<Message> iter = f.getMessages().iterator(); iter.hasNext();) {
						Message m = iter.next();

						if(m.getMessageType() != null) {
							switch((((IntegerID) m.getMessageType().getID()).intValue())) {
							case 1511758069:
							case 18362:

							// a herb was found in a region
							case 1349776898:

								// a certain amount of herbs has been detected in a region
								if((m.getAttributes() != null) && m.getAttributes().containsKey("region")) {
									String str = (String) m.getAttributes().get("region");
									CoordinateID coord = CoordinateID.parse(str, ",");

									if(coord == null) {
										coord = CoordinateID.parse(str, " ");
									}

									Region r = data.getRegion(coord);

									if(r != null) {
										String value = (String) m.getAttributes().get("herb");

										if(value != null) {
											ItemType type = data.rules.getItemType(StringID.create(value),
																				   true);
											r.setHerb(type);
										}

										if((((IntegerID) m.getMessageType().getID()).intValue()) == 1349776898) {
											// a certain amount of herbs has been detected in a region
											String amount = (String) m.getAttributes().get("amount");

											if(amount != null) {
												r.setHerbAmount(amount);
											}
										}
									}
								}

								break;
							}
						}
					}
				}
			}
		}

		// there can be dummy units (UnitContainer owners and such), find and remove these
		if(data.units() != null) {
			Collection<ID> dummyUnitIDs = new LinkedList<ID>();

			for(Iterator iter = data.units().values().iterator(); iter.hasNext();) {
				Unit unit = (Unit) iter.next();

				if(unit.getName() == null) {
					dummyUnitIDs.add(unit.getID());
				}
			}

			for(Iterator iter = dummyUnitIDs.iterator(); iter.hasNext();) {
				data.units().remove(iter.next());
			}
		}

		/* retrieve the temp units mentioned in the orders and
		 create them as TempUnit objects */
		int sortIndex = 0;
		List<Unit> sortedUnits = new LinkedList<Unit>(data.units().values());
		Collections.sort(sortedUnits, new SortIndexComparator<Unique>(IDComparator.DEFAULT));

		for(Iterator unitIter = sortedUnits.iterator(); unitIter.hasNext();) {
			Unit unit = (Unit) unitIter.next();
			unit.setSortIndex(sortIndex++);
			sortIndex = unit.extractTempUnits(sortIndex);
		}

		/* 'known' information does not necessarily show up in the
		report. e.g. depleted region resources are not mentioned
		although we actually know that the resource is available with
		an amount of 0. Resolve this ambiguity here: */
		if((data != null) && (data.regions() != null)) {
			/*ItemType sproutResourceID = */data.rules.getItemType("Schößlinge",true);
			/*ItemType treeResourceID = */data.rules.getItemType("Bäume",true);
			/*ItemType mallornSproutResourceID = */data.rules.getItemType("Mallornschößlinge",true);
			/*ItemType mallornTreeResourceID = */data.rules.getItemType("Mallorn",true);

			for(Iterator regionIter = data.regions().values().iterator(); regionIter.hasNext();) {
				Region region = (Region) regionIter.next();

				/* first determine whether we know everything about
				this region */
				if(!region.units().isEmpty()) {
					/* now patch as much missing information as
					possible */
					// FIXME (stm) 2006-10-28: this has bitten us already
					// check what is visible in what visibility 
					// (lighthouse, neigbbour, travel)   
					
					// the following tags seem to be present under undefined visibility even if they are zero :
					// Bauern, Silber, Unterh, Rekruten, Pferde, (Lohn)
					if (region.getVisibility()==null){
						if(region.getPeasants() < 0) {
							region.setPeasants(0);
						}

						if(region.getSilver() < 0) {
							region.setSilver(0);
						}

						if(region.getWage() < 0) {
							// TODO: should we set this to 10 instead?
							region.setWage(0);
						}

						if(region.getHorses() < 0) {
							region.setHorses(0);
						}
					}
					// ------------------------------------------------------------------
					// the following tags seem to be visible for "lighthouse";visibility:
					// DURCHSCHIFFUNG
					// SCHIFF: Name, Beschr, Typ, Groesse, Kapitaen, Partei, (Kueste)
					// EINHEIT: Name, Beschr, Partei, Anderepartei, typprefix, Typ, Anzahl, Schiff, (Burg)
					// GEGENSTÄNDE
					// ------------------------------------------------------------------
					// the following tags seem to be visible for "travel";visibility:
					// Baeume, Schoesslinge, Bauern, Pferde, Effects 
					// DURCHSCHIFFUNG
					// DURCHREISE
					// BURG: Typ, Name, Beschr, Groesse, Besitzer, Partei
					// SCHIFF: Name, Beschr, Typ, Groesse, Kapitaen, Partei, Kueste
					// EINHEIT: Name, Beschr, Partei, Anderepartei, typprefix, Typ, Anzahl, Burg, Schiff
					// ------------------------------------------------------------------
					// the following tags seem to be visible even vor "neighbour";visibilty:
					// Name, Terrain, Beschr 
					
					if(region.getSprouts() < 0) {
						region.setSprouts(0);
					}
					/**
					if(data.rules != null) {
						// FIXME: we should finally remove this code, shouldn't we?
						// 2002.05.21 pavkovic:
						// first of all: Remove resource information for sprouts, trees,
						// mallornsprouts and mallorntrees!
						// this is cumbersome, and will only stay for some time (two months)
						// to get rid of double or triple entries of these resources
						//
						boolean cleanup = true;

						if(cleanup) {
							Set cleanupSet = CollectionFactory.createHashSet();

							for(Iterator riter = region.resources().iterator(); riter.hasNext();) {
								RegionResource rr = (RegionResource) riter.next();

								if(rr.getType().equals(sproutResourceID) ||
									   rr.getType().equals(treeResourceID) ||
									   rr.getType().equals(mallornSproutResourceID) ||
									   rr.getType().equals(mallornTreeResourceID)) {
									cleanupSet.add(rr.getType());
								}
							}

							for(Iterator riter = cleanupSet.iterator(); riter.hasNext();) {
								ItemType type = (ItemType) riter.next();
								region.removeResource(type);
							}
						}

						if(region.mallorn) {
							// pavkovic 2002.05.06: remove disjunct resource (trees and sprouts)
							region.removeResource(sproutResourceID);

							// add new resource
							if(region.getResource(mallornSproutResourceID) == null) {
								RegionResource res = new RegionResource(LongID.create(mallornSproutResourceID.hashCode()),
																		mallornSproutResourceID);
								res.setAmount(region.sprouts);
								region.addResource(res);
							}
						} else {
							// pavkovic 2002.05.06: remove disjunct resource (trees and sprouts)
							region.removeResource(mallornSproutResourceID);

							// add new resource
							if(region.getResource(sproutResourceID) == null) {
								RegionResource res = new RegionResource(LongID.create(sproutResourceID.hashCode()),
																		sproutResourceID);
								res.setAmount(region.sprouts);
								region.addResource(res);
							}
						}
					}
					**/
					if(region.getTrees() < 0) {
						region.setTrees(0);
					}
					/**
					if(region.mallorn) {
						// pavkovic 2002.05.06: remove disjunct resource (trees and sprouts)
						region.removeResource(treeResourceID);

						// add new resource
						if(region.getResource(mallornTreeResourceID) == null) {
							RegionResource res = new RegionResource(LongID.create(mallornTreeResourceID.hashCode()),
																	mallornTreeResourceID);
							res.setAmount(region.trees);
							region.addResource(res);
						}
					} else {
						// pavkovic 2002.05.06: remove disjunct resource (trees and sprouts)
						region.removeResource(mallornTreeResourceID);

						// add new resource
						if(data.rules != null) {
							if(region.getResource(treeResourceID) == null) {
								RegionResource res = new RegionResource(LongID.create(treeResourceID.hashCode()),
																		treeResourceID);
								res.setAmount(region.trees);
								region.addResource(res);
							}
						}
					}
					**/

				}
			}
		}
	}

	/**
	 * DOCUMENT-ME
	 *
	 * 
	 */
	public void postProcessAfterTrustlevelChange(GameData data) {
		// initialize fog-of-war cache (FIXME(pavkovic): Do it always?)
		// clear all fog-of-war caches
		if(data.regions() != null) {
			for(Iterator iter = data.regions().values().iterator(); iter.hasNext();) {
				Region r = (Region) iter.next();
				r.setFogOfWar(-1);
			}
		}

		// intialize the fog-of-war cache for all regions that are covered by lighthouses
		if(data.buildings() != null) {
			BuildingType type = data.rules.getBuildingType(EresseaConstants.B_LIGHTTOWER);
			RegionType oceanType = data.rules.getRegionType(EresseaConstants.RT_OCEAN);
			Comparator<Sorted> sortIndexComparator = new SortIndexComparator<Unique>(IDComparator.DEFAULT);

			if(type != null) {
				for(Iterator iter = data.buildings().values().iterator(); iter.hasNext();) {
					Building b = (Building) iter.next();

					if(type.equals(b.getType()) && (b.getSize() >= 10)) {
						int personCounter = 0;
						int perceptionSkillLevel = 0;
						List<Unit> sortedInmates = new LinkedList<Unit>(b.units());
						Collections.sort(sortedInmates, sortIndexComparator);

						for(Iterator<Unit> inmates = sortedInmates.iterator(); inmates.hasNext() && (personCounter < 4); personCounter++) {
							Unit inmate = inmates.next();
							Skill perceptionSkill = inmate.getSkill(data.rules.getSkillType(EresseaConstants.S_WAHRNEHMUNG,
																							true));

							if(perceptionSkill != null) {
								perceptionSkillLevel = Math.max(perceptionSkill.getLevel(),
																perceptionSkillLevel);
							}
						}

						int maxRadius = (int) Math.min((Math.log(b.getSize()) / Math.log(10)) + 1,
													   perceptionSkillLevel / 3);

						if(maxRadius > 0) {
							Map regions = Regions.getAllNeighbours(data.regions(),
																   b.getRegion().getCoordinate(),
																   maxRadius, null);

							for(Iterator regionIter = regions.values().iterator();
									regionIter.hasNext();) {
								Region r = (Region) regionIter.next();

								if((oceanType == null) || oceanType.equals(r.getType())) {
									r.setFogOfWar(0);
								}
							}
						}
					}
				}
			}
		}

		// intialize the fog-of-war cache for all regions where units or ships traveled through
		for(Iterator iterator = data.regions().values().iterator(); iterator.hasNext();) {
			Region r = (Region) iterator.next();

			if(r.getTravelThru() != null) {
				initTravelThru(data, r, r.getTravelThru());
			}

			if(r.getTravelThruShips() != null) {
				initTravelThru(data, r, r.getTravelThruShips());
			}
		}
	}

	private void initTravelThru(GameData data, Region region, Collection travelThru) {
		for(Iterator iter = travelThru.iterator(); iter.hasNext();) {
			Message mes = (Message) iter.next();

			// fetch ID of Unit or Ship from Message of type "<name> (<id>)"
			String s = mes.getText();
			int startpos = s.lastIndexOf("(") + 1;
			int endpos = s.length() - 1;

			if((startpos > -1) && (endpos > startpos)) {
				try {
					ID id = EntityID.createEntityID(s.substring(startpos, endpos),data.base);

					if((data.getUnit(id) != null) &&
						   (data.getUnit(id).getFaction().isPrivileged())) {
						// fast return
						region.setFogOfWar(0);

						return;
					} else {
						Ship ship = data.getShip(id);

						if(ship != null) {
							for(Iterator i = ship.units().iterator(); i.hasNext();) {
								if(((Unit) i.next()).getFaction().isPrivileged()) {
									// fast return
									region.setFogOfWar(0);

									return;
								}
							}
						}
					}
				} catch(NumberFormatException e) {
				}
			}
		}
	}
}
