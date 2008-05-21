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

package magellan.library;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import magellan.library.gamebinding.MovementEvaluator;
import magellan.library.relation.ItemTransferRelation;
import magellan.library.relation.ReserveRelation;
import magellan.library.relation.UnitRelation;
import magellan.library.rules.ItemType;
import magellan.library.rules.Race;
import magellan.library.rules.SkillType;
import magellan.library.utils.Cache;
import magellan.library.utils.Sorted;
import magellan.library.utils.Taggable;

/**
 * This is a Unit. A unit is an object that contains one or more persons.
 * All persons in this unit have the same level of skills.
 * 
 * @author $Author: $
 * @version $Revision: 389 $
 */
public interface Unit extends Related, HasRegion, Sorted, Taggable {
  /** The unit does not possess horses */
  public static final int CAP_NO_HORSES = MovementEvaluator.CAP_NO_HORSES;

  /** The unit is not sufficiently skilled in horse riding */
  public static final int CAP_UNSKILLED = MovementEvaluator.CAP_UNSKILLED;

  /** hmmm.... */  // Do not know either (Fiete)
  // OK, mail from enno: different bit coded guard effects
  public static final int GUARDFLAG_TAX = 1;
  public static final int GUARDFLAG_MINING = 2;
  public static final int GUARDFLAG_WOOD = 4;
  public static final int GUARDFLAG_TRAVELTHRU = 8;
  public static final int GUARDFLAG_LANDING = 16;
  public static final int GUARDFLAG_CREWS = 32;
  public static final int GUARDFLAG_RECRUIT = 64;
  public static final int GUARDFLAG_PRODUCE = 128;
  /**
  Original Mail:
    Das ist was anderes: Je nach Rasse kann Bewachung einen untershciedlichen Effekt haben. Bergwaechter zum Beispiel bewachen den Abbau von Eisen/Steinen, so dass niemand etwas abbauen kann solange sie das tun, Elfen haben das fuer Holz, usw. Das sieht man allerdings nicht im CR.

    Folgende defines hat der Server dafuer:
    #define GUARD_TAX 1
      /* Verhindert Steuereintreiben *
    #define GUARD_MINING 2
      /* Verhindert Bergbau *
    #define GUARD_TREES 4
      /* Verhindert Waldarbeiten *
    #define GUARD_TRAVELTHRU 8
      /* Blockiert Durchreisende *
    #define GUARD_LANDING 16
      /* Verhindert Ausstieg + Weiterreise * 
    *#define GUARD_CREWS 32
      /* Verhindert Unterhaltung auf Schiffen
       * * #define GUARD_RECRUIT 64
       /* Verhindert Rekrutieren *
    #define GUARD_PRODUCE 128
      /* Verhindert Abbau von Resourcen mit RTF_LIMITED *
    */
  
  /**
   * Returns true if no orders are set
   */
  public boolean ordersAreNull();

  /**
   * Returns true if the orders has changed
   */
  public boolean ordersHaveChanged();

  /**
   * @see #ordersHaveChanged()
   */
  public void setOrdersChanged(boolean changed);

  /**
   * Clears the orders and refreshes the relations
   */
  public void clearOrders();

  /**
   * Clears the orders and possibly refreshes the relations
   * 
   * @param refreshRelations
   *          if true also refresh the relations of the unit.
   */
  public void clearOrders(boolean refreshRelations);

  /**
   * Removes the order at position <tt>i</tt> and refreshes the relations
   */
  public void removeOrderAt(int i);

  /**
   * Removes the order at position <tt>i</tt> and possibly refreshes the
   * relations
   * 
   * @param refreshRelations
   *          if true also refresh the relations of the unit.
   */
  public void removeOrderAt(int i, boolean refreshRelations);

  /**
   * Adds the order at position <tt>i</tt> and refreshes the relations
   * 
   * @param i
   *          An index between 0 and getOrders().getSize() (inclusively)
   * @param newOrders
   */
  public void addOrderAt(int i, String newOrders);

  /**
   * Adds the order at position <tt>i</tt> and possibly refreshes the
   * relations
   * 
   * @param i
   *          An index between 0 and getOrders().getSize() (inclusively)
   * @param newOrders
   * @param refreshRelations
   *          if true also refresh the relations of the unit.
   */
  public void addOrderAt(int i, String newOrders, boolean refreshRelations);

  /**
   * Adds the order and refreshes the relations
   * 
   * @param newOrders
   */
  public void addOrders(String newOrders);

  /**
   * Adds the order and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations
   *          if true also refresh the relations of the unit.
   */
  public void addOrders(String newOrders, boolean refreshRelations);

  /**
   * Adds the orders and refreshes the relations
   * 
   * @param newOrders
   */
  public void addOrders(Collection<String> newOrders);

  /**
   * Adds the orders and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations
   *          If true also refresh the relations of the unit
   */
  public void addOrders(Collection<String> newOrders, boolean refreshRelations);

  /**
   * Sets the orders and refreshes the relations
   * 
   * @param newOrders
   */
  public void setOrders(Collection<String> newOrders);

  /**
   * Sets the orders and possibly refreshes the relations
   * 
   * @param newOrders
   * @param refreshRelations
   *          if true also refresh the relations of the unit.
   */
  public void setOrders(Collection<String> newOrders, boolean refreshRelations);

  /**
   * Delivers a readonly collection of alle orders of this unit.
   */
  public Collection<String> getOrders();

  /**
   * Sets the group this unit belongs to.
   * 
   * @param g
   *          the group of the unit
   */
  public void setGroup(Group g);

  /**
   * Returns the group this unit belongs to.
   * 
   * @return the group this unit belongs to
   */
  public Group getGroup();

  /**
   * Sets an alias id for this unit.
   * 
   * @param id
   *          the alias id for this unit
   */
  public void setAlias(UnitID id);

  /**
   * Returns the alias, i.e. the id of this unit it had in the last turn (e.g.
   * after a NUMMER order).
   * 
   * @return the alias or null, if the id did not change.
   */
  public UnitID getAlias();

  /**
   * Returns the item of the specified type if the unit owns such an item. If
   * not, null is returned.
   */
  public Item getItem(ItemType type);

  /**
   * Sets whether is unit really belongs to its unit or only pretends to do so.
   */
  public void setSpy(boolean bool);

  /**
   * Returns whether this unit only pretends to belong to its faction.
   * 
   * @return true if the unit is identified as spy
   */
  public boolean isSpy();

  /**
   * Sets the faction this unit pretends to belong to. A unit cannot disguise
   * itself as a different faction and at the same time be a spy of another
   * faction, therefore, setting a value other than null results in having the
   * spy attribute set to false.
   */
  public void setGuiseFaction(Faction f);

  /**
   * Returns the faction this unit pretends to belong to. If the unit is not
   * disguised null is returned.
   */
  public Faction getGuiseFaction();

  /**
   * Adds an item to the unit. If the unit already has an item of the same type,
   * the item is overwritten with the specified item object.
   * 
   * @return the specified item i.
   */
  public Item addItem(Item i);

  /**
   * Sets the temp id this unit had before becoming a real unit.
   */
  public void setTempID(UnitID id);

  /**
   * Returns the id the unit had when it was still a temp unit. This id is only
   * set in the turn after the unit turned from a temp unit into to a real unit.
   * 
   * @return the temp id or null, if this unit was no temp unit in the previous
   *         turn.
   */
  public UnitID getTempID();

  /**
   * Sets the region this unit is in. If this unit already has a different
   * region set it removes itself from the collection of units in that region.
   */
  public void setRegion(Region r);

  /**
   * Returns the region this unit is staying in.
   */
  public Region getRegion();

  /**
   * Sets the faction for this unit. If this unit already has a different
   * faction set it removes itself from the collection of units in that faction.
   */
  public void setFaction(Faction faction);

  /**
   * Returns the faction this unit belongs to.
   */
  public Faction getFaction();

  /**
   * Sets the building this unit is staying in. If the unit already is in
   * another building this method removes it from the unit collection of that
   * building.
   */
  public void setBuilding(Building building);

  /**
   * Returns the building this unit is staying in.
   */
  public Building getBuilding();

  /**
   * Sets the ship this unit is on. If the unit already is on another ship this
   * method removes it from the unit collection of that ship.
   */
  public void setShip(Ship ship);

  /**
   * Returns the ship this unit is on.
   */
  public Ship getShip();

  /**
   * Sets an index indicating how instances of class are sorted in the report.
   */
  public void setSortIndex(int index);

  /**
   * Returns an index indicating how instances of class are sorted in the
   * report.
   */
  public int getSortIndex();

  /**
   * Sets the unit dependent prefix for the race name.
   */
  public void setRaceNamePrefix(String prefix);

  /**
   * Returns the unit dependent prefix for the race name.
   */
  public String getRaceNamePrefix();

  /**
   * Returns the name of this unit's race including the prefixes of itself, its
   * faction and group if it has such and those prefixes are set.
   * 
   * @param data
   *          The GameData
   * @return the name or null if this unit's race or its name is not set.
   */
  public String getRaceName(GameData data);

  /**
   * @return The String of the RealRace. If no RealRace is known( = null) the
   *         normal raceName is returned.
   */
  public String getRealRaceName();

  /**
   * Delivers the info "typ" from CR without any prefixes and translations used
   * for displaying the according race icon
   * 
   * @return Name of the race
   */
  public String getSimpleRaceName();

  /**
   * Returns the child temp units created by this unit's orders.
   */
  public Collection<TempUnit> tempUnits();

  /**
   * Return the child temp unit with the specified ID.
   */
  public Unit getTempUnit(ID key);

  /**
   * Clears the list of temp units created by this unit. Clears only the caching
   * collection, does not perform clean-up like deleteTemp() does.
   */
  public void clearTemps();

  /**
   * Returns alle orders including the orders necessary to issue the creation of
   * all the child temp units of this unit.
   */
  public List<String> getCompleteOrders();

  /**
   * Returns a list of all orders.
   * 
   * @param writeUnitTagsAsVorlageComment
   */
  public List<String> getCompleteOrders(boolean writeUnitTagsAsVorlageComment);
  
  /**
   * Creates a new temp unit with this unit as the parent. The temp unit is
   * fully initialised, i.e. it is added to the region units collection in the
   * specified game data,it inherits the faction, building or ship, region,
   * faction stealth status, group, race and combat status settings and adds
   * itself to the corresponding unit collections.
   * 
   * @throws IllegalArgumentException
   */
  public TempUnit createTemp(ID key);
  
  /**
   * Removes a temp unit with this unit as the parent completely from the game
   * data.
   */
  public void deleteTemp(ID key, GameData data);

  /**
   * @see magellan.library.Named#getModifiedName()
   */
  public String getModifiedName();

  /**
   * Adds a relation to this unit.
   */
  public void addRelation(UnitRelation rel);

  /**
   * Removes a relation to this unit.
   */
  public UnitRelation removeRelation(UnitRelation rel);
  
  /**
   * deliver all directly related units
   */
  public void getRelatedUnits(Collection<Unit> units);
  
  /**
   * Recursively retrieves all units that are related to this unit via one of
   * the specified relations.
   * 
   * @param units
   *          all units gathered so far to prevent loops.
   * @param relations
   *          a set of classes naming the types of relations that are eligible
   *          for regarding a unit as related to some other unit.
   */
  public void getRelatedUnits(Set<Unit> units, Set relations);
  
  /**
   * Returns a List of the reached coordinates of the units movement starting
   * with the current region or an empty list if unit is not moving.
   * 
   * @return A list of coordinates, empty list means no movement
   */
  public List<CoordinateID> getModifiedMovement();
  
  /**
   * Returns the modified ship if this unit modifies the ship
   */
  public Ship getModifiedShip();

  /**
   * Returns the modified building if this unit modifies it
   */
  public Building getModifiedBuilding();

  /**
   * Returns the modified skills if this will change.
   */
  public Skill getModifiedSkill(SkillType type);
  
  /**
   * Returns the skills of this unit as they would appear after the orders for
   * person transfers are processed.
   */
  public Collection<Skill> getModifiedSkills();
  
  /**
   * Returns the unit container this belongs to. (ship, building or null)
   */
  public UnitContainer getUnitContainer();
  
  /**
   * Returns the modified unit container this unit belongs to. (ship, building
   * or null)
   */
  public UnitContainer getModifiedUnitContainer();
  
  /**
   * Returns the skill of the specified type if the unit has such a skill, else
   * null is returned.
   */
  public Skill getSkill(SkillType type);
  
  public boolean isSkillsCopied();

  public void setSkillsCopied(boolean skillsCopied);

  /**
   * Adds a skill to unit's collection of skills. If the unit already has a
   * skill of the same type it is overwritten with the the new skill object.
   * 
   * @return the specified skill s.
   */
  public Skill addSkill(Skill s);
  
  /**
   * Returns all skills this unit has.
   * 
   * @return a collection of Skill objects.
   */
  public Collection<Skill> getSkills();
  public Map<ID,Skill> getSkillMap();
  
  /**
   * Removes all skills from this unit.
   */
  public void clearSkills();

  /**
   * Returns all the items this unit possesses.
   * 
   * @return a collection of Item objects.
   */
  public Collection<Item> getItems();
  public void setItems(Map<ID,Item> items);
  public Map<ID,Item> getItemMap();

  /**
   * Removes all items from this unit.
   */
  public void clearItems();

  /**
   * Returns the item of the specified type as it would appear after the orders
   * of this unit have been processed, i.e. the amount of the item might be
   * modified by transfer orders. If the unit does not have an item of the
   * specified type nor is given one by some other unit, null is returned.
   */
  public Item getModifiedItem(ItemType type);

  /**
   * Returns a collection of the reserve relations concerning the given Item.
   * 
   * @param itemType
   * @return a collection of ReserveRelation objects.
   */
  public Collection<ReserveRelation> getItemReserveRelations(ItemType itemType);

  /**
   * Returns a collection of the itemrelations concerning the given Item.
   * 
   * @return a collection of ItemTransferRelation objects.
   */
  public List<ItemTransferRelation> getItemTransferRelations(ItemType type);

  /**
   * Returns a collection of the personrelations associated with this unit
   * 
   * @return a collection of PersonTransferRelation objects.
   */
  public List getPersonTransferRelations();

  /**
   * Returns the items of this unit as they would appear after the orders of
   * this unit have been processed.
   * 
   * @return a collection of Item objects.
   */
  public Collection<Item> getModifiedItems();

  /**
   * Returns the number of persons in this unit.
   */
  public int getPersons();

  /**
   * Returns the number of persons in this unit as it would be after the orders
   * of this and other units have been processed since it may be modified by
   * transfer orders.
   */
  public int getModifiedPersons();
  
  /**
   * Returns the new combat status of this unit as it would be after the orders
   * of this unit have been processed.
   */
  public int getModifiedCombatStatus();
  
  /**
   * Returns the new (expected) guard value of this unit as it would be after the orders of this unit
   * (and the unit is still allive next turn)
   * (@TODO: do we need a region.getModifiedGuards - List? guess and hope not)
   */
  public int getModifiedGuard();
  
  /**
   * Returns the new Unaided status of this unit as it would be after the orders of this unit
   * 
   */
  public boolean getModifiedUnaided();

  /**
   * @return true if weight is well known and NOT evaluated by Magellan
   */
  public boolean isWeightWellKnown();

  /**
   * Returns the overall weight of this unit (persons and items) in silver
   */
  public int getWeight();
  
  /**
   * Returns the maximum payload in silver of this unit when it travels by
   * horse. Horses, carts and persons are taken into account for this
   * calculation. If the unit has a sufficient skill in horse riding but there
   * are too many carts for the horses, the weight of the additional carts are
   * also already considered.
   * 
   * @return the payload in silver, CAP_NO_HORSES if the unit does not possess
   *         horses or CAP_UNSKILLED if the unit is not sufficiently skilled in
   *         horse riding to travel on horseback.
   */
  public int getPayloadOnHorse();

  /**
   * Returns the maximum payload in silver of this unit when it travels on foot.
   * Horses, carts and persons are taken into account for this calculation. If
   * the unit has a sufficient skill in horse riding but there are too many
   * carts for the horses, the weight of the additional carts are also already
   * considered. The calculation also takes into account that trolls can tow
   * carts.
   * 
   * @return the payload in silver, CAP_UNSKILLED if the unit is not
   *         sufficiently skilled in horse riding to travel on horseback.
   */
  public int getPayloadOnFoot();

  /**
   * Returns the weight of all items of this unit that are not horses or carts
   * in silver
   */
  public int getLoad();

  /**
   * Returns the weight of all items of this unit that are not horses or carts
   * in silver based on the modified items.
   */
  public int getModifiedLoad();

  /**
   * Returns the number of regions this unit is able to travel within one turn
   * based on the riding skill, horses, carts and load of this unit.
   */
  public int getRadius();

  /**
   * Returns the overall weight (persons, items) of this unit in silver based on
   * the modified items and persons.
   */
  public int getModifiedWeight();

  /**
   * Returns all units this unit is transporting as passengers.
   * 
   * @return A Collection of transported <code>Unit</code>s
   */
  public Collection<Unit> getPassengers();

  /**
   * Returns all units indicating by their orders that they would transport this
   * unit as a passenger (if there is more than one such unit, that is a
   * semantical error of course).
   * 
   * @return A Collection of <code>Unit</code>s carrying this one
   */
  public Collection<Unit> getCarriers();

  /**
   * Returns a Collection of all the units that are taught by this unit.
   * 
   * @return A Collection of <code>Unit</code>s taught by this unit
   */
  public Collection<Unit> getPupils();

  /**
   * Returns a Collection of all the units that are teaching this unit.
   * 
   * @return A Collection of <code>Unit</code>s teaching this unit
   */
  public Collection<Unit> getTeachers();

  /**
   * Returns a list of attacked victims.
   */
  public Collection<Unit> getAttackVictims();
  /**
   * Returns a list of attackers.
   */
  public Collection<Unit> getAttackAggressors();
  
  /**
   * Parses the orders of this unit and detects relations between units
   * established by those orders. When does this method have to be called? No
   * relation of a unit can affect an object outside the region that unit is in.
   * So when all relations regarding a certain unit as target or source need to
   * be determined, this method has to be called for each unit in the same
   * region. Since relations are defined by unit orders, modified orders may
   * lead to different relations. Therefore refreshRelations() has to be invoked
   * on a unit after its orders were modified.
   */
  public void refreshRelations();

  /**
   * Parses the orders of this unit <i>beginning at the <code>from</code>th
   * order</i> and detects relations between units established by those orders.
   * When does this method have to be called? No relation of a unit can affect
   * an object outside the region that unit is in. So when all relations
   * regarding a certain unit as target or source need to be determined, this
   * method has to be called for each unit in the same region. Since relations
   * are defined by unit orders, modified orders may lead to different
   * relations. Therefore refreshRelations() has to be invoked on a unit after
   * its orders were modified.
   * 
   * @param from
   *          Start from this line
   */
  public void refreshRelations(int from);

  /**
   * Returns a String representation of this unit.
   */
  public String toString();
  
  /**
   * @param withName
   */
  public String toString(boolean withName);

  /**
   * Add a order to the unit's orders. This function ensures that TEMP units are
   * not affected by the operation.
   * 
   * @param order
   *          the order to add.
   * @param replace
   *          if <tt>true</tt>, the order replaces any other of the unit's
   *          orders of the same type. If <tt>false</tt> the order is simply
   *          added.
   * @param length
   *          denotes the number of tokens that need to be equal for a
   *          replacement. E.g. specify 2 if order is "BENENNE EINHEIT abc" and
   *          all "BENENNE EINHEIT" orders should be replaced but not all
   *          "BENENNE" orders.
   * @return <tt>true</tt> if the order was successfully added.
   */
  public boolean addOrder(String order, boolean replace, int length);

  /**
   * Scans this unit's orders for temp units to create. It constructs them as
   * TempUnit objects and removes the corresponding orders from this unit. Uses
   * the default order locale to parse the orders.
   * 
   * @param tempSortIndex
   *          an index for sorting units (required to reconstruct the original
   *          order in the report) which is incremented with each new temp unit.
   * @return the new sort index. <tt>return value</tt> - sortIndex is the
   *         number of temp units read from this unit's orders.
   */
  public int extractTempUnits(int tempSortIndex);

  /**
   * Scans this unit's orders for temp units to create. It constructs them as
   * TempUnit objects and removes the corresponding orders from this unit.
   * 
   * @param tempSortIndex
   *          an index for sorting units (required to reconstruct the original
   *          order in the report) which is incremented with each new temp unit.
   * @param locale
   *          the locale to parse the orders with.
   * @return the new sort index. <tt>return value</tt> - sortIndex is the
   *         number of temp units read from this unit's orders.
   */
  public int extractTempUnits(int tempSortIndex, Locale locale);

  /**
   * Returns the value of aura.
   * 
   * @return Returns aura.
   */
  public int getAura();

  /**
   * Sets the value of aura.
   *
   * @param aura The value for aura.
   */
  public void setAura(int aura);

  /**
   * Returns the value of auraMax.
   * 
   * @return Returns auraMax.
   */
  public int getAuraMax();

  /**
   * Sets the value of auraMax.
   *
   * @param auraMax The value for auraMax.
   */
  public void setAuraMax(int auraMax);

  /**
   * Returns the value of cache.
   * 
   * @return Returns cache.
   */
  public Cache getCache();

  /**
   * Sets the value of cache.
   *
   * @param cache The value for cache.
   */
  public void setCache(Cache cache);

  /**
   * Returns the value of combatSpells.
   * 
   * @return Returns combatSpells.
   */
  public Map<ID, CombatSpell> getCombatSpells();

  /**
   * Sets the value of combatSpells.
   *
   * @param combatSpells The value for combatSpells.
   */
  public void setCombatSpells(Map<ID, CombatSpell> combatSpells);

  /**
   * Returns the value of combatStatus.
   * 
   * @return Returns combatStatus.
   */
  public int getCombatStatus();

  /**
   * Sets the value of combatStatus.
   *
   * @param combatStatus The value for combatStatus.
   */
  public void setCombatStatus(int combatStatus);

  /**
   * Returns the value of comments.
   * 
   * @return Returns comments.
   */
  public List<String> getComments();

  /**
   * Sets the value of comments.
   *
   * @param comments The value for comments.
   */
  public void setComments(List<String> comments);

  /**
   * Returns the value of effects.
   * 
   * @return Returns effects.
   */
  public List<String> getEffects();

  /**
   * Sets the value of effects.
   *
   * @param effects The value for effects.
   */
  public void setEffects(List<String> effects);

  /**
   * Returns the value of familiarmageID.
   * 
   * @return Returns familiarmageID.
   */
  public ID getFamiliarmageID();

  /**
   * Sets the value of familiarmageID.
   *
   * @param familiarmageID The value for familiarmageID.
   */
  public void setFamiliarmageID(ID familiarmageID);

  /**
   * Returns the value of follows.
   * 
   * @return Returns follows.
   */
  public Unit getFollows();

  /**
   * Sets the value of follows.
   *
   * @param follows The value for follows.
   */
  public void setFollows(Unit follows);

  /**
   * Returns the value of guard.
   * 
   * @return Returns guard.
   */
  public int getGuard();

  /**
   * Sets the value of guard.
   *
   * @param guard The value for guard.
   */
  public void setGuard(int guard);

  /**
   * Returns the value of health.
   * 
   * @return Returns health.
   */
  public String getHealth();

  /**
   * Sets the value of health.
   *
   * @param health The value for health.
   */
  public void setHealth(String health);

  /**
   * Returns the value of hideFaction.
   * 
   * @return Returns hideFaction.
   */
  public boolean isHideFaction();

  /**
   * Sets the value of hideFaction.
   *
   * @param hideFaction The value for hideFaction.
   */
  public void setHideFaction(boolean hideFaction);

  /**
   * Returns the value of isHero.
   * 
   * @return Returns isHero.
   */
  public boolean isHero();

  /**
   * Sets the value of isHero.
   *
   * @param isHero The value for isHero.
   */
  public void setHero(boolean isHero);

  /**
   * Returns the value of isStarving.
   * 
   * @return Returns isStarving.
   */
  public boolean isStarving();

  /**
   * Sets the value of isStarving.
   *
   * @param isStarving The value for isStarving.
   */
  public void setStarving(boolean isStarving);

  /**
   * Returns the value of ordersConfirmed.
   * 
   * @return Returns ordersConfirmed.
   */
  public boolean isOrdersConfirmed();

  /**
   * Sets the value of ordersConfirmed.
   *
   * @param ordersConfirmed The value for ordersConfirmed.
   */
  public void setOrdersConfirmed(boolean ordersConfirmed);
  /**
   * Returns the value of privDesc.
   * 
   * @return Returns privDesc.
   */
  public String getPrivDesc();

  /**
   * Sets the value of privDesc.
   *
   * @param privDesc The value for privDesc.
   */
  public void setPrivDesc(String privDesc);

  /**
   * Returns the value of race.
   * 
   * @return Returns race.
   */
  public Race getRace();

  /**
   * Sets the value of race.
   *
   * @param race The value for race.
   */
  public void setRace(Race race);

  /**
   * Returns the value of realRace.
   * 
   * @return Returns realRace.
   */
  public Race getRealRace();

  /**
   * Sets the value of realRace.
   *
   * @param realRace The value for realRace.
   */
  public void setRealRace(Race realRace);

  /**
   * Returns the value of siege.
   * 
   * @return Returns siege.
   */
  public Building getSiege();

  /**
   * Sets the value of siege.
   *
   * @param siege The value for siege.
   */
  public void setSiege(Building siege);
  
  /**
   * Returns the value of spells.
   * 
   * @return Returns spells.
   */
  public Map<ID, Spell> getSpells();

  /**
   * Sets the value of spells.
   *
   * @param spells The value for spells.
   */
  public void setSpells(Map<ID, Spell> spells);

  /**
   * Returns the value of stealth.
   * 
   * @return Returns stealth.
   */
  public int getStealth();

  /**
   * Sets the value of stealth.
   *
   * @param stealth The value for stealth.
   */
  public void setStealth(int stealth);

  /**
   * Returns the value of unaided.
   * 
   * @return Returns unaided.
   */
  public boolean isUnaided();

  /**
   * Sets the value of unaided.
   *
   * @param unaided The value for unaided.
   */
  public void setUnaided(boolean unaided);

  /**
   * Returns the value of unitMessages.
   * 
   * @return Returns unitMessages.
   */
  public List<Message> getUnitMessages();

  /**
   * Sets the value of unitMessages.
   *
   * @param unitMessages The value for unitMessages.
   */
  public void setUnitMessages(List<Message> unitMessages);

  /**
   * Sets the value of persons.
   *
   * @param persons The value for persons.
   */
  public void setPersons(int persons);

  /**
   * Sets the value of skills.
   *
   * @param skills The value for skills.
   */
  public void setSkills(Map<ID, Skill> skills);

  /**
   * Sets the value of weight.
   *
   * @param weight The value for weight.
   */
  public void setWeight(int weight);

}
