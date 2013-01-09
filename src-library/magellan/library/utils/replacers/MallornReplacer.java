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

package magellan.library.utils.replacers;

import magellan.library.Region;
import magellan.library.RegionResource;
import magellan.library.rules.ItemType;
import magellan.library.utils.Resources;

/**
 * Returns mallorn amount
 * 
 * @author Fiete
 * @version 1.0
 */
public class MallornReplacer extends AbstractRegionReplacer {
  private ItemType resourceType;

  /**
   * @param resourceType
   */
  public MallornReplacer(ItemType resourceType) {
    this.resourceType = resourceType;
  }

  /**
   * Returns the mallorn of the region.
   * 
   * @see magellan.library.utils.replacers.AbstractRegionReplacer#getRegionReplacement(magellan.library.Region)
   */
  @Override
  public Object getRegionReplacement(Region region) {
    if (resourceType == null)
      return Integer.valueOf(0);
    if (!region.isMallorn())
      return Integer.valueOf(0);
    RegionResource mallornResource = region.getResource(resourceType);
    if (mallornResource == null)
      return Integer.valueOf(0);
    return Integer.valueOf(mallornResource.getAmount());
  }

  public String getDescription() {
    return Resources.get("util.replacers.mallornreplacer.description");
  }

}
