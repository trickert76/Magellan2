// class magellan.library.gamebinding.UnitMaintenanceOrder
// created on Jun 19, 2012
//
// Copyright 2003-2012 by magellan project team
//
// Author : $Author: $
// $Id: $
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//
package magellan.library.gamebinding;

import java.util.Collections;

import magellan.library.GameData;
import magellan.library.Order;
import magellan.library.Region;
import magellan.library.Unit;
import magellan.library.utils.OrderToken;

/**
 * Virtual order for handling reserve orders
 *
 * @author stm
 */
public class ReserveOwnOrder extends SimpleOrder {

  protected Region region;

  /**
   * @param r
   */
  public ReserveOwnOrder(Region r) {
    super(Collections.singletonList(new OrderToken(OrderToken.TT_EOC)), "");
    region = r;
  }

  /**
   * Every unit with reserve orders reserves its own items.
   *
   * @see magellan.library.gamebinding.SimpleOrder#execute(magellan.library.gamebinding.ExecutionState,
   *      magellan.library.GameData, magellan.library.Unit, int)
   */
  @Override
  public void execute(ExecutionState state, GameData data, Unit unit, int line) {
    if (unit != null)
      throw new RuntimeException("meta order should not be called with unit");
    for (Unit u : region.units()) {
      for (Order order : u.getOrders2()) {
        if (order instanceof ReserveOrder) {
          ((ReserveOrder) order).setOwn(true);
          order.execute(state, data, u, line);
          ((ReserveOrder) order).setOwn(false);
        }
      }
    }

  }

}
