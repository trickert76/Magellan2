/*
 * Copyright (C) 2000-2004 Roger Butenuth, Andreas Gampe, Stefan Goetz, Sebastian Pappert, Klaas
 * Prause, Enno Rehling, Sebastian Tusk, Ulrich Kuester, Ilja Pavkovic This file is part of the
 * Eressea Java Code Base, see the file LICENSING for the licensing information applying to this
 * file.
 */

package magellan.library.gamebinding;

import java.util.List;

import magellan.library.Region;
import magellan.library.Unit;

/**
 * DOCUMENT-ME
 * 
 * @author $Author: $
 * @version $Revision: 171 $
 */
public interface MovementEvaluator {
  /** The unit does not possess horses */
  public static final int CAP_NO_HORSES = Integer.MIN_VALUE;

  /* The unit is not sufficiently skilled in horse riding */

  /** DOCUMENT-ME */
  public static final int CAP_UNSKILLED = MovementEvaluator.CAP_NO_HORSES + 1;

  /**
   * Returns the maximum payload in GE 100 of this unit when it travels by horse. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered.
   * 
   * @return the payload in GE 100, CAP_NO_HORSES if the unit does not possess horses or
   *         CAP_UNSKILLED if the unit is not sufficiently skilled in horse riding to travel on
   *         horseback.
   */
  public int getPayloadOnHorse(Unit unit);

  /**
   * Returns the maximum payload in GE 100 of this unit when it travels on foot. Horses, carts and
   * persons are taken into account for this calculation. If the unit has a sufficient skill in
   * horse riding but there are too many carts for the horses, the weight of the additional carts
   * are also already considered. The calculation also takes into account that trolls can tow carts.
   * 
   * @return the payload in GE 100, CAP_UNSKILLED if the unit is not sufficiently skilled in horse
   *         riding to travel on horseback.
   */
  public int getPayloadOnFoot(Unit unit);

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver
   */
  public int getLoad(Unit unit);

  /**
   * Returns the weight of all items of this unit that are not horses or carts in silver based on
   * the modified items.
   */
  public int getModifiedLoad(Unit unit);

  /**
   * The initial weight of the unit as it appears in the report. This should be the game dependent
   * version used to calculate the weight if the information is not available in the report.
   * 
   * @return the weight of the unit in silver (GE 100).
   */
  public int getWeight(Unit unit);

  /**
   * The modified weight is calculated from the modified number of persons and the modified items.
   * Due to some game dependencies this is done in this class.
   * 
   * @return the modified weight of the unit in silver (GE 100).
   */
  public int getModifiedWeight(Unit unit);

  /**
   * Returns the unit's speed based on payload and horses.
   * 
   * @param unit
   * @return
   */
  public int getModifiedRadius(Unit unit);

  /**
   * Returns the number of regions this unit is able to travel (on roads if <code>onRoad</code> is
   * <code>true</code>) within one turn based on modified riding skill, horses, carts and load of
   * this unit.
   */
  public int getModifiedRadius(Unit unit, boolean onRoad);

  /**
   * Returns the number of regions this unit is able to travel within one turn based on modified
   * riding skill, horses, carts, load of this unit and roads <i>on the given path</i>.
   * 
   * @param unit
   * @param path A sequence of regions. The first region must be the current region of the unit. If
   *          two successive elements of the path are the same region, this is interpreted as a
   *          PAUSE, which always ends a turn. See {@link Unit#getModifiedMovement()}.
   * @return The number of regions, the unit may move on this path. The result is always
   *         <code><= path.size()-1</code>.
   * @throws IllegalArgumentException if the unit is not in the first path region or the path is not
   *           continuous
   */
  public int getModifiedRadius(Unit unit, List<Region> path);

  /**
   * Returns the number of turns that the unit needs to travel on the specified path based on
   * modified riding skill, horses, carts, load of this unit and roads <i>on the given path</i>.
   * 
   * @param unit
   * @param path A sequence of regions. The first region must be the current region of the unit.
   * @return
   */
  public int getDistance(Unit unit, List<Region> path);

  /**
   * Returns the number of regions this unit is able to travel within one turn based on the riding
   * skill, horses, carts and load of this unit.
   * 
   * @deprecated Use {@link #getModifiedRadius(Unit)}.
   */
  @Deprecated
  public int getRadius(Unit u);

  /**
   * Returns the number of regions this unit is able to travel (on roads if <code>onRoad</code> is
   * <code>true</code>) within one turn based on the riding skill, horses, carts and load of this
   * unit.
   * 
   * @deprecated Use {@link #getModifiedRadius(Unit, boolean)}.
   */
  @Deprecated
  public int getRadius(Unit u, boolean onRoad);
}
