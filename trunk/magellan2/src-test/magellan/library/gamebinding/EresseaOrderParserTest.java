// class magellan.library.gamebinding.EresseaOrderParserTest
// created on Jun 20, 2009
//
// Copyright 2003-2009 by magellan project team
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
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program (see doc/LICENCE.txt); if not, write to the
// Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
package magellan.library.gamebinding;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;
import java.util.NoSuchElementException;

import magellan.client.completion.AutoCompletion;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.gamebinding.EresseaOrderParser.ArbeiteReader;
import magellan.library.gamebinding.EresseaOrderParser.AttackReader;
import magellan.library.gamebinding.EresseaOrderParser.OrderHandler;
import magellan.library.gamebinding.EresseaOrderParser.TokenBucket;
import magellan.library.utils.OrderToken;
import magellan.library.utils.Resources;
import magellan.test.GameDataBuilder;
import magellan.test.MagellanTestWithResources;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests if valid orders are parsed correctly
 * 
 * @author stm
 * @version 1.0, Jun 20, 2009
 */
public class EresseaOrderParserTest extends MagellanTestWithResources {

  private static boolean DO_KNOWN_FAILURES = System.getenv("MAGELLAN2_TESTING") == null ? false
      : System.getenv("MAGELLAN2_TESTING").equalsIgnoreCase("INTERNAL");
  protected GameData data;
  private EresseaOrderParser parser;
  protected AutoCompletion completion;
  private EresseaOrderCompleter completer;
  protected GameDataBuilder builder;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    builder = new GameDataBuilder();
    data = builder.createSimpleGameData();

    Region region = data.getRegions().iterator().next();
    Faction faction = data.getFactions().iterator().next();
    builder.addBuilding(data, region, "burg", "Burg", "gro�e Burg", 200);
    builder.addShip(data, region, "ship", "Langboot", "ein Langboot", 50);
    builder.addUnit(data, "zwei", "Zweite", faction, region);

    setParser(new EresseaOrderParser(data));
    completion = new AutoCompletion(context);
    setCompleter(new EresseaOrderCompleter(data, completion));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#EresseaOrderParser(magellan.library.GameData, magellan.library.gamebinding.EresseaOrderCompleter)}
   * .
   */
  @Test
  public void testEresseaOrderParserGameDataEresseaOrderCompleter() {
    EresseaOrderParser localParser = new EresseaOrderParser(data, getCompleter());
    assertTrue(localParser.getData() == data);
    assertTrue(localParser.getCompleter() == getCompleter());
    assertSame(61, localParser.getCommands().size());
    assertSame(61, localParser.getHandlers().size());
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#initCommands()}.
   */
  @Test
  public void testInitCommands() {
    assertSame(61, getParser().getCommands().size());
    assertSame(61, getParser().getHandlers().size());
    assertTrue(getParser().getCommands().contains("WORK"));
    assertTrue(getParser().getCommands().contains("DESTROY"));
    assertTrue(getParser().getCommands().contains("SABOTAGE"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#addCommand(java.lang.String, magellan.library.gamebinding.EresseaOrderParser.OrderHandler)}
   * .
   */
  @Test
  public void testAddCommand() {
    OrderHandler fooHandler = getParser().new OrderHandler() {
      @Override
      protected boolean readIt(OrderToken token) {
        return false;
      }
    };
    assertFalse(getParser().getCommands().contains("foo"));
    assertFalse(getParser().getHandlers().contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("foo")).size() == 0);
    getParser().addCommand("foo", fooHandler);
    assertTrue(getParser().getCommands().contains("foo"));
    assertTrue(getParser().getHandlers().contains(fooHandler));

    assertTrue(getParser().getHandlers(new OrderToken("orders.foo")).contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("orders.foo")).size() == 1);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#removeCommand(java.lang.String)}.
   */
  @Test
  public void testRemoveCommand() {
    OrderHandler fooHandler = getParser().new OrderHandler() {
      @Override
      protected boolean readIt(OrderToken token) {
        return false;
      }
    };
    getParser().addCommand("foo", fooHandler);
    assertTrue(getParser().getCommands().contains("foo"));
    assertTrue(getParser().getHandlers().contains(fooHandler));

    assertTrue(getParser().getHandlers(new OrderToken("orders.foo")).contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("orders.foo")).size() == 1);
    getParser().removeCommand("foo");
    assertFalse(getParser().getCommands().contains("foo"));
    assertFalse(getParser().getHandlers().contains(fooHandler));
    assertTrue(getParser().getHandlers(new OrderToken("orders.foo")).size() == 0);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @Test
  public void testGetNextToken() {
    assertTrue(getParser().getLastToken() == null);
    assertFalse(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 0);

    getParser().read(new StringReader("123 abc"));

    assertTrue(getParser().hasNextToken());
    assertTrue(equals(getParser().getLastToken(), new OrderToken("123", 0, 3, OrderToken.TT_UNDEF,
        true)));
    assertTrue(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 1);

    OrderToken token = getParser().getNextToken();

    assertTrue(token.getText().equals("abc"));
    assertTrue(getParser().hasNextToken());
    assertEquals(getParser().getLastToken(), token);
    assertEquals(getParser().getTokenIndex(), 2);

    assertTrue(equals(getParser().getNextToken(), new OrderToken(OrderToken.TT_EOC)));
    assertFalse(getParser().hasNextToken());
    assertEquals(getParser().getTokenIndex(), 3);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#getNextToken()}.
   */
  @Test(expected = NullPointerException.class)
  public void testGetNextTokenNull() {
    assertTrue(getParser().getNextToken() != null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#read(java.io.Reader)}.
   */
  @Test
  public void testRead() {
    checkOrder(""); // FIXME ???!!!
    checkOrder("AR");
    checkOrder("A", false);
  }

  // /**
  // * Test method for
  // * {@link
  // magellan.library.gamebinding.EresseaOrderParser#readOrder(magellan.library.utils.OrderToken)}
  // * .
  // */
  // @Test
  // public void testReadOrder() {
  // fail("Not yet implemented");
  // }

  protected void checkOrder(String string) {
    checkOrder(string, true);
  }

  protected void checkOrder(String string, boolean result) {
    boolean retVal = getParser().read(new StringReader(string));
    assertEquals("checking " + string, result, retVal);
  }

  @Test
  public void testCommentReader() {
    checkOrder(EresseaConstants.O_PCOMMENT);
    checkOrder("//");
    checkOrder("// ");
    checkOrder("// HALLO");
    checkOrder("//;", false);
    checkOrder("//ARBEITE", false);
    checkOrder("///", false);
    checkOrder("////", false);

    checkOrder("");
    checkOrder(" ");
    checkOrder(";");
    checkOrder("; askj baskjdb");
    checkOrder(";;;");
    checkOrder(" ; abc");
  }

  /**
   * Testmethod for ARBEITE
   */
  @Test
  public void testArbeiteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_WORK));
    checkOrder("ARBEITE");
    checkOrder("ARBEITEN");
    checkOrder("arbeiten");
    checkOrder("AR");
    checkOrder("ARBEITE ;");
    checkOrder("arbeitene", false);
    checkOrder("ARBEISEN", false);
    checkOrder("ARBEITE 1", false);
    checkOrder(" ARBEITE ARBEITE", false);
    checkOrder(" ARBEITE");
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.AtReader}.
   */
  @Test
  public void testAtReader() {
    checkOrder("@ARBEITE");
    checkOrder(" @ARBEITE");
    checkOrder("@", false);
    checkOrder("@@ARBEITE", false); // server actually accepts this and turn it into @ARBEITE...
    checkOrder("@@", false);
    checkOrder("@ARBEITE 1", false);
    checkOrder(";@", true);
    checkOrder("@; ", false);
    checkOrder("@  ; ", false);
    checkOrder("@// ", false); // Server actually accepts this; dodgy...
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.AttackReader}.
   */
  @Test
  public void testAttackReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ATTACK) + " 123");
    checkOrder("ATTACKIERE 123");
    checkOrder("ATTACKIERE xyz");
    checkOrder("ATTACKIERE TEMP xyz"); // this is actually legal
    checkOrder("ATTACKIERE xyz; abc");
    checkOrder("ATTACKIERE abcde", false);
    checkOrder("ATTACKIERE", false);
    checkOrder("ATTACKIERE 123 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BannerReader}.
   */
  @Test
  public void testBannerReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_BANNER) + "\"\"");
    checkOrder("BANNER  \"abc\"");
    checkOrder("BANNER  \"abc\"; bla");
    checkOrder("BANNER  'abc'");
    checkOrder("BANNER  abc", false);
    checkOrder("BANNER  \"abc\" \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeansprucheReader}.
   */
  @Test
  public void testBeansprucheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CLAIM) + " Sonnensegel");
    checkOrder("BEANSPRUCHE 2 Sonnensegel");
    checkOrder("BEANSPRUCHE \"Sch�nes Geschenk\"");
    checkOrder("BEANSPRUCHE Sch�nes~Geschenk");
    checkOrder("BEANSPRUCHE", false);
    checkOrder("BEANSPRUCHE 1", false); // ??
    checkOrder("BEANSPRUCHE Sch�nes Geschenk", false); // ??
    checkOrder("BEANSPRUCHE 1 2 Sonnensegel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BefoerderungReader}.
   */
  @Test
  public void testBefoerderungReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PROMOTION));
    checkOrder("BEF�RDERUNG");
    checkOrder("BEF�RDER");
    checkOrder("BEF�RDERUNG ;");
    checkOrder("BEF�RDERE", false);
    checkOrder("BEF�RDERUNG 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeklaueReader}.
   */
  @Test
  public void testBeklaueReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_STEAL) + " 123");
    checkOrder("BEKLAUE 1");
    checkOrder("BEKLAUE abc;");
    checkOrder("BEKLAUE TEMP abc;"); // TODO is this legal?
    checkOrder("BEKLAUE ;abc", false);
    checkOrder("BEKLAUE \"abc\"", false);
    checkOrder("BEKLAUE", false);
    checkOrder("BEKLAUE anc foo", false);
    checkOrder("BEKLAUE 20 foo", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BelagereReader}.
   */
  @Test
  public void testBelagereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SIEGE) + " burg");
    checkOrder("BELAGERE burg");
    checkOrder("BELAGERE abc", false);
    checkOrder("BELAGERUNG burg", false);
    checkOrder("BELAGERE TEMP burg", false);
    checkOrder("BELAGERE burg 123", false);
    checkOrder("BELAGERE 2 burg", false);
    checkOrder("BELAGERE \"burg\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenenneReader}.
   */
  @Test
  public void testBenenneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_NAME) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Geb�ude", "S�gewerk",
        "SCHIFF", "REGION" }) {
      checkOrder("BENENNEN " + thing + " \"Foo\"");
      checkOrder("BENENNE " + thing + " \"Foo\"; comment");
      checkOrder("BENENNE " + thing + " \"\"", false);
      checkOrder("BENENNE " + thing + " abc", false);
      checkOrder("BENENNE " + thing + " 123 \"abc\"", false);
      checkOrder("BENENNE " + thing + " \"abc\" 123", false);
    }

    checkOrder("BENENNE FREMDE EINHEIT zwei \"abc\"");
    checkOrder("BENENNE FREMDE EINHEIT zwei \"\"", false);
    checkOrder("BENENNE FREMDE PARTEI zwei \"abc\"", true); // no such faction
    checkOrder("BENENNE FREMDE BURG burg \"abc\"");
    checkOrder("BENENNE FREMDE SCHIFF ship \"abc\"");

    // for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "Geb�ude", "S�gewerk",
    // "SCHIFF" }) {
    // checkOrder("BENENNEN FREMDE " + thing + " 123 \"Foo\"");
    // checkOrder("BENENNE FREMDE " + thing + " abc \"Foo\"; comment");
    // checkOrder("BENENNE FREMDE " + thing + " TEMP xyz \"Foo\""); // this is really allowed (for
    // // units)
    // checkOrder("BENENNE FREMDE " + thing + " xyz \"\"", false);
    // checkOrder("BENENNE FREMDE " + thing + " abc abc", false);
    // checkOrder("BENENNE FREMDE " + thing + " 123 123 \"abc\"", false);
    // checkOrder("BENENNE FREMDE " + thing + " 123 \"abc\" 123", false);
    // }

    checkOrder("BENENNE abc \"abc\"", false);

    // FIXME: these ambiguous commands shouldn't be accepted (maybe?)
    if (DO_KNOWN_FAILURES) {
      for (String thing : new String[] { "S", "E" }) {
        checkOrder("BENENNEN " + thing + " \"Foo\"", false); // is it SCHIFF or S�gewerk or
                                                             // Steinbruch??
        checkOrder("BENENNE " + thing + " \"Foo\"; comment", false);
      }
    }
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BenutzeReader}.
   */
  @Test
  public void testBenutzeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_USE) + " Siebenmeilentee");
    checkOrder("BENUTZEN Wasser~des~Lebens");
    checkOrder("BENUTZEN \"Wasser des Lebens\"");
    checkOrder("BENUTZEN 22 Wasser~des~Lebens");
    checkOrder("BENUTZEN 22 \"Wasser des Lebens\"");
    checkOrder("BENUTZEN 22 \"Wasser des Lebens\"; \"123\"");
    checkOrder("BENUTZEN  11 22 Wasser~des~Lebens", false);
    checkOrder("BENUTZEN  22 Siebenmeilentee 3", false);
    checkOrder("BENUTZEN  22 Siebenmeilentee Heiltrank", false);
    checkOrder("BENUTZEN  BENUTZEN", true);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BeschreibeReader}.
   */
  @Test
  public void testBeschreibeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DESCRIBE) + " EINHEIT \"Foo\"");
    for (String thing : new String[] { "EINHEIT", "PRIVAT", "BURG", "S�gewerk", "SCHIFF", "REGION" }) {
      checkOrder("BESCHREIBEN " + thing + " \"Foo\"");
      checkOrder("BESCHREIBE " + thing + " \"Foo\"; comment");
      checkOrder("BESCHREIBE " + thing + " \"\"", true);
      checkOrder("BESCHREIBE " + thing + " abc", false);
      checkOrder("BESCHREIBE " + thing + " 123 \"abc\"", false);
      checkOrder("BESCHREIBE " + thing + " \"abc\" 123", false);
    }
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BetreteReader}.
   */
  @Test
  public void testBetreteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ENTER) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_SHIP) + " ship");
    checkOrder("BetreteN BURG burg");
    checkOrder("BetreteN BURG burg; ");
    checkOrder("BetreteN BURG abc", true); // no such building
    checkOrder("BetreteN BURG TEMP 123", false);
    checkOrder("BetreteN BURG \"burg\"", false);
    checkOrder("BetreteN 1 BURG burg", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BewacheReader}.
   */
  @Test
  public void testBewacheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GUARD));
    checkOrder("BEWACHE");
    checkOrder("BEWACHE ; a");
    checkOrder("BEWACHE 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.BotschaftReader}.
   */
  @Test
  public void testBotschaftReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MESSAGE) + " "
        + Resources.get(EresseaConstants.O_REGION) + " \"hallo\"");
    for (String thing : new String[] { "EINHEIT", "PARTEI", "BURG", "S�gewerk", "SCHIFF", "REGION" }) {
      String nr = " abc ";
      if (thing.equals("REGION")) {
        nr = "";
      }
      checkOrder("BOTSCHAFT " + thing + nr + " \"Foo\"");
      checkOrder("BOTSCHAFT " + thing + nr + " \"Foo\"; comment");
      checkOrder("BOTSCHAFT " + thing + nr + " \"\"", false);
      checkOrder("BOTSCHAFT " + thing + nr + " abc", false);
      checkOrder("BOTSCHAFT " + thing + nr + " 123 \"abc\"", false);
      checkOrder("BOTSCHAFT " + thing + nr + " \"abc\" 123", false);
    }
    checkOrder("BOTSCHAFT EINHEIT TEMP 123 \"Bar\"", true); // TODO is this really allowed?
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.DefaultReader}.
   */
  @Test
  public void testDefaultReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DEFAULT) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_WORK));
    checkOrder("DEFAULT \"ARBEITEN\"");
    checkOrder("DEFAULT 'ARBEITEN'");
    checkOrder("DEFAULT 'LERNEN Ausdauer'");
    checkOrder("DEFAULT 'LERNEN Alchemie'");
    checkOrder("DEFAULT 'LERNEN Alchemie 200'");
    checkOrder("DEFAULT 'BANNER \"abc\"'");
    checkOrder("DEFAULT 'BANNER \"abc def\"'");
    checkOrder("DEFAULT 'BANNER 'abc def''", false);
    checkOrder("DEFAULT 'LERNEN'", false);
    checkOrder("DEFAULT LERNEN Ausdauer", false);
    // TODO more checks
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.EmailReader}.
   */
  @Test
  public void testEmailReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_EMAIL) + " \"a@b.com\"");
    checkOrder("EMAIL \'a@b.com\'");
    checkOrder("EMAIL \"123@456.com\"");
    checkOrder("EMAIL \"eressea-server@eressea.upb.de\"");
    checkOrder("Email \"enno@world\"");
    checkOrder("EMAIL \"abc\"", false);
    checkOrder("EMAIL \"www.eressea.de\"", false);
    checkOrder("EMAIL stm@example.com", false);
    checkOrder("EMAIL \"hallo@world.com", false);
    checkOrder("EMAIL ", false);
    checkOrder("EMAIL \"\"", false);
    checkOrder("EMAIL \"abc@def.ghi\" \"jkl@mno.pqr\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.EndeReader}.
   */
  @Test
  public void testEndeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_END));
    checkOrder("ENDE 123", false);
    checkOrder("ENDE \"123\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FahreReader}.
   */
  @Test
  public void testFahreReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RIDE) + " zwei");
    checkOrder("FAHREN zwei");
    checkOrder("FAHREN TEMP 456");
    checkOrder("FAHREN abc"); // invisible unit allowed
    checkOrder("FAHREN abcde", false);
    checkOrder("FAHREN zwei zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FolgeReader}.
   */
  @Test
  public void testFolgeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FOLLOW) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_UNIT) + " zwei");
    checkOrder("FOLGEN SCHIFF ship");
    checkOrder("FOLGEN SCHIFF 123", false);
    checkOrder("FOLGEN SCHIFF TEMP 123", false);
    checkOrder("FOLGEN EINHEIT 123 456", false);
    checkOrder("FOLGEN EINHEIT \"abc\"", false);
    checkOrder("FOLGEN EINHEIT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ForscheReader}.
   */
  @Test
  public void testForscheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESEARCH) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("FORSCHE", false);
    checkOrder("FORSCHE KR�UTER 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GibReader}.
   */
  @Test
  public void testSupplyReader() {
    // there is an undocumented supply order in Eressea
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SUPPLY) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GibReader}.
   */
  @Test
  public void testGibReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GIVE) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("GIB KR�UTER", false);
    checkOrder("GIB abc \"KR�UTER\"", false);
    checkOrder("GIB 123 KR�UTER 123", false);

    checkOrder("GIB 123 KOMMANDO");
    checkOrder("GIB TEMP 123 KOMMANDO");
    checkOrder("GIB 0 KOMMANDO", false);
    checkOrder("GIB KOMMANDO", false);
    checkOrder("GIB abc \"KOMMANDO\"", false);
    checkOrder("GIB 123 KOMMANDO 123", false);
    checkOrder("GIB 123 123 KOMMANDO", false);

    checkOrder("GIB 123 2 Silber");
    checkOrder("GIB 0 2 Silber");
    checkOrder("GIB 123 ALLES Silber");
    checkOrder("GIB 123 ALLES \"Silber\"");
    checkOrder("GIB TEMP 123 ALLES \"Silber\"");
    checkOrder("GIB 123 123 123 Silber", false);
    checkOrder("GIB 123 2 Silber 123", false);
    checkOrder("GIB 123 123 123 Silber", false);

    checkOrder("GIB 123 ALLES PERSONEN");
    checkOrder("GIB 0 ALLES PERSONEN");
    checkOrder("GIB 123 2 PERSONEN");
    checkOrder("GIB TEMP 123 2 PERSONEN");
    checkOrder("GIB 123 ALLES PERSONEN 2", false);
    checkOrder("GIB 123 123 ALLES PERSONEN", false);
    checkOrder("GIB 123 ALLES 123 PERSONEN", false);

    checkOrder("GIB 123 EINHEIT");
    checkOrder("GIB 0 EINHEIT", false);

    checkOrder("GIB 123 2 Holz");
    checkOrder("GIB TEMP 123 2 Holz");
    checkOrder("GIB 0 2 Holz");
    checkOrder("GIB 123 2 \"Holz\"");
    checkOrder("GIB 123 2 W�rziger~Wagemut");
    checkOrder("GIB 123 ALLES Holz");
    checkOrder("GIB 123 2 W�rziger Wagemut", false);

    checkOrder("GIB 123 2 W�rziger~Wagemut");

    checkOrder("GIB 123 2 EINHEIT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.GruppeReader}.
   */
  @Test
  public void testGruppeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GROUP));
    checkOrder("GRUPPE abc");
    checkOrder("GRUPPE \"Die wilden Kerle\"");
    checkOrder("GRUPPE Hallo~Welt");
    checkOrder("GRUPPE Hallo Welt", false);
    checkOrder("GRUPPE 123 123", false);
    checkOrder("GRUPPE TEMP 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.HelfeReader}.
   */
  @Test
  public void testHelfeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_HELP) + " 123 "
        + Resources.getOrderTranslation(EresseaConstants.O_ALL));
    checkOrder("HELFE 123 ALLES NICHT");
    checkOrder("HELFEN 123 GIB");
    checkOrder("HELFEN 123 GIB NICHT");
    checkOrder("HELFE 123 K�MPFE");
    checkOrder("HELFE 123 BEWACHEN");
    checkOrder("HELFE 123 SILBER");
    checkOrder("HELFE 123 PARTEITARNUNG");
    checkOrder("HELFE 123 bla", false);
    checkOrder("HELFE abcde GIB", false);
    checkOrder("HELFE 123 GIB BLA", false);
    checkOrder("HELFE 123 456", false);
    checkOrder("HELFE TEMP 456", false);
    checkOrder("HELFE 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaempfeReader}.
   */
  @Test
  public void testKaempfeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_COMBAT));
    checkOrder("K�MPFE AGGRESSIV");
    checkOrder("K�MPFE HINTEN");
    checkOrder("K�MPFE DEFENSIV");
    checkOrder("K�MPFE NICHT");
    checkOrder("K�MPFE FLIEHE");
    checkOrder("K�MPFE HELFE");
    checkOrder("K�MPFE HELFE NICHT");
    checkOrder("K�MPFE VORNE", false); // deprecated
    checkOrder("K�MPFE AGGRESSIV NICHT", false);
    checkOrder("K�MPFE VORNE HINTEN", false);
    checkOrder("K�MPFE 123 HINTEN", false);
    checkOrder("K�MPFE FLIEHE NICHT", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KampfzauberReader}.
   */
  @Test
  public void testKampfzauberReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_COMBATSPELL) + " Hagel");
    checkOrder("KAMPFZAUBER STUFE 2 Hagel");
    checkOrder("KAMPFZAUBER Hagel NICHT");
    checkOrder("KAMPFZAUBER STUFE 2 \"Gro�es Fest\"", false); // no combat spell
    checkOrder("KAMPFZAUBER STUFE Hagel", false);
    checkOrder("KAMPFZAUBER Magisches Gescho�", false);
    checkOrder("KAMPFZAUBER Hagel 123", false);
    checkOrder("KAMPFZAUBER STUFE x Hagel", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KaufeReader}.
   */
  @Test
  public void testKaufeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_BUY) + " 2 Balsam");
    checkOrder("KAUFE 2 �l");
    checkOrder("KAUFE 2 Oel");
    checkOrder("KAUFE Weihrauch", false);
    checkOrder("KAUFE 2 Schnickschnak", false);
    checkOrder("KAUFE 2 �l Weihrauch", false);
    checkOrder("KAUFE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.KontaktiereReader}.
   */
  @Test
  public void testKontaktiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CONTACT) + " 123");
    checkOrder("KONTAKTIERE a");
    checkOrder("KONTAKTIERE TEMP a");
    checkOrder("KONTAKTIERE abc def", false);
    checkOrder("KONTAKTIERE", false);
    checkOrder("KONTAKTIERE \"abc\"", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LehreReader}.
   */
  @Test
  public void testLehreReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_TEACH) + " abc");
    checkOrder("LEHRE abc 123 456 TEMP zyx");
    checkOrder("LEHRE abc Hiebwaffen", false);
    checkOrder("LEHRE", false);
    checkOrder("LEHRE Hiebwaffen", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LerneReader}.
   */
  @Test
  public void testLerneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LEARN) + " Ausdauer");
    checkOrder("LERNE Hiebwaffen");
    checkOrder("LERNE \"Hiebwaffen\"");
    checkOrder("LERNE Waffenloser~Kampf");
    checkOrder("LERNE", false);
    checkOrder("LERNE foo", false);
    checkOrder("LERNE Waffenloser Kampf", false);
    checkOrder("LERNE 123 456", false);
    checkOrder("LERNE Magie 1234#", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.LocaleReader}.
   */
  @Test
  public void testLocaleReader() {
    // this is a valid order, but not /inside/ a unit
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LOCALE), false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.MacheReader}.
   */
  @Test
  public void testMacheReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MAKE) + " TEMP 123");
    checkOrder("MACHE BURG");
    checkOrder("MACHE 2 BURG");
    checkOrder("MACHE BURG 123");
    checkOrder("MACHE 3 BURG abc");
    checkOrder("MACHE S�gewerk");
    checkOrder("MACHE 2 S�gewerk");
    checkOrder("MACHE Boot");
    checkOrder("MACHE Trireme");
    checkOrder("MACHE SCHIFF");
    checkOrder("MACHE SCHIFF 123");
    checkOrder("MACHE 2 SCHIFF 123");
    checkOrder("MACHE STRASSE no");
    checkOrder("MACHE 2 STRASSE no");
    checkOrder("MACHE STRASSE nordwesten");
    checkOrder("MACHE KR�UTER");
    checkOrder("MACHE 2 KR�UTER");
    checkOrder("MACHE Pferd");
    checkOrder("MACHE Pferd Pferd", false);
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE \"S�gewerk\"", false); // well...
    checkOrder("MACHE \"Pferd\"");
    checkOrder("MACHE 2 Schwert");
    checkOrder("MACHE 2 \"Rostiger Zweih�nder\"");
    checkOrder("MACHE 1 Pferd");
    checkOrder("MACHE", false); // actually, this is correct, but dangerous
    checkOrder("MACHE BURGG", false);
    checkOrder("MACHE a BURG", false);
    checkOrder("MACHE a BURG 123", false);
    checkOrder("MACHE Boot 123", false);
    checkOrder("MACHE 3 Trireme a", false);
    checkOrder("MACHE a Trireme", false);
    checkOrder("MACHE SCHIFF abc def", false);
    checkOrder("MACHE STRASSE s", false);
    checkOrder("MACHE 2 STRASSE", false);
    checkOrder("MACHE STRASSE", false);
    checkOrder("MACHE KR�UTER abc", false);
    checkOrder("MACHE Pferd abc", false);
    checkOrder("MACHE a Pferd", false);
    checkOrder("MACHE a Hurz", false);
    checkOrder("MACHE a Rostiger Bih�nder", false);
    checkOrder("MACHE 2 Wasser~des~Lebens"); // TODO
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NachReader}.
   */
  @Test
  public void testNachReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_MOVE) + " westen");
    checkOrder("NACH o");
    checkOrder("NACH so");
    checkOrder("NACH sw");
    checkOrder("NACH w");
    checkOrder("NACH nw");
    checkOrder("NACH no");
    checkOrder("NACH osten");
    checkOrder("NACH s�dosten");
    checkOrder("NACH suedosten");
    checkOrder("NACH westen");
    checkOrder("NACH nordw");
    checkOrder("NACH nordo");
    checkOrder("NACH o so sw w nw no o o");
    checkOrder("NACH 1 o", false);
    checkOrder("NACH", false);
    checkOrder("NACH 1", false);
    checkOrder("NACH o PAUSE", false);
  }

  // new FinalKeywordReader());
  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NeustartReader}.
   */
  @Test
  public void testNeustartReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESTART) + " Trolle \"passwort\"");
    checkOrder("NEUSTART Zwerge \"\"", false);
    checkOrder("NEUSTART Zwerge", false);
    checkOrder("NEUSTART", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.NummerReader}.
   */
  @Test
  public void testNummerReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_NUMBER) + " EINHEIT");
    checkOrder("NUMMER EINHEIT 123");
    checkOrder("NUMMER PARTEI 123");
    checkOrder("NUMMER SCHIFF 123");
    checkOrder("NUMMER BURG 123");
    checkOrder("NUMMER PARTEI");
    checkOrder("NUMMER", false);
    checkOrder("NUMMER EINHEIT 123 123", false);
    checkOrder("NUMMER EINHEIT abcdefg", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.OptionReader}.
   */
  @Test
  public void testOptionReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_OPTION) + " AUSWERTUNG");
    checkOrder("OPTION PUNKTE NICHT");
    checkOrder("OPTION PUNKTE NICHT MEHR", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ParteiReader}.
   */
  @Test
  public void testParteiReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FACTION), false); // TODO???
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PasswortReader}.
   */
  @Test
  public void testPasswortReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PASSWORD) + " \"squiggy\"");
    checkOrder("PASSWORT", true);
    checkOrder("PASSWORT 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PflanzeReader}.
   */
  @Test
  public void testPflanzeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PLANT) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_HERBS));
    checkOrder("PFLANZE B�UME");
    checkOrder("PFLANZE MALLOrnSamen");
    checkOrder("PFLANZE SAMEN");
    checkOrder("PFLANZE 4 B�UME");
    checkOrder("PFLANZE 5 MALLOrnSamen");
    checkOrder("PFLANZE 2 SAMEN");
    checkOrder("PFLANZE", false);
    checkOrder("PFLANZE 2", false);
    checkOrder("PFLANZE ", false);
    checkOrder("PFLANZE Silber", false);
    checkOrder("PFLANZE 3 SAMEN NICHT", false);
    checkOrder("PFLANZE 123 123 SAMEN", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PiraterieReader}.
   */
  @Test
  public void testPiraterieReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PIRACY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.PraefixReader}.
   */
  @Test
  public void testPraefixReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_PREFIX));
    checkOrder("PR�FIX Nebel");
    checkOrder("PR�FIX Blubb"); // do not currently test for allowed prefixes
    checkOrder("PR�FIX Bla blubb", false);
    checkOrder("PR�FIX 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RegionReader}.
   */
  @Test
  public void testRegionReader() {
    // this is a valid order, but not /inside/ a unit
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_REGION) + " 1,1", false);
    // FIXME read comma'd coordinate
    /*
     * checkOrder(Resources.getOrderTranslation(EresseaConstants.O_REGION) + " 1,1");
     * checkOrder("REGION", false); checkOrder("REGION 1 3", false); checkOrder("REGION 123",
     * false); checkOrder("REGION abc,def", false);
     */
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RekrutiereReader}.
   */
  @Test
  public void testRekrutiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RECRUIT) + " 1");
    checkOrder("REKRUTIERE 5");
    checkOrder("REKRUTIERE 0", true); // TODO should we return false here?
    checkOrder("REKRUTIERE", false);
    checkOrder("REKRUTIERE 1 2", false);
    checkOrder("REKRUTIERE 1 Zwerg", false); // for E2, this is an error
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ReserviereReader}.
   */
  @Test
  public void testReserviereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_RESERVE) + " 1 "
        + data.rules.getItemType(EresseaConstants.I_USILVER).getName());
    checkOrder("RESERVIEREN ALLES Holz");
    checkOrder("RESERVIERE 2 Silber");
    checkOrder("RESERVIERE JE 1 Holz");
    checkOrder("RESERVIERE 1 Flabberghast"); // item does not exist
    checkOrder("RESERVIEREN ALLES", false);
    checkOrder("RESERVIEREN JE", false);
    checkOrder("RESERVIEREN JE 2", false);
    checkOrder("RESERVIEREN JE 2 ALLES", false);
    checkOrder("RESERVIERE JE Holz", false);
    checkOrder("RESERVIERE 1.5 Holz", false);
    checkOrder("RESERVIERE 2 1 Holz", false);
    checkOrder("RESERVIERE 1 1", false);
    checkOrder("RESERVIERE", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.RouteReader}.
   */
  @Test
  public void testRouteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ROUTE) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_NE));
    checkOrder("ROUTE o");
    checkOrder("ROUTE so");
    checkOrder("ROUTE sw");
    checkOrder("ROUTE w");
    checkOrder("ROUTE nw");
    checkOrder("ROUTE no");
    checkOrder("ROUTE osten");
    checkOrder("ROUTE s�dosten");
    checkOrder("ROUTE suedosten");
    checkOrder("ROUTE westen");
    checkOrder("ROUTE nordw");
    checkOrder("ROUTE nordo");
    checkOrder("ROUTE o so sw w nw no o o");
    checkOrder("ROUTE o PAUSE");
    checkOrder("ROUTE o PAUSE w pause pause");
    checkOrder("ROUTE PAUSE w pause pause");
    checkOrder("ROUTE 1 o", false);
    checkOrder("ROUTE", false);
    checkOrder("ROUTE 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SortiereReader}.
   */
  @Test
  public void testSortiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SORT) + " "
        + Resources.getOrderTranslation(EresseaConstants.O_BEFORE) + " " + "123");
    checkOrder("SORTIERE VOR abc");
    checkOrder("SORTIERE HINTER abc");
    checkOrder("SORTIERE VOR ", false);
    checkOrder("SORTIERE", false);
    checkOrder("SORTIERE abc abc", false);
    checkOrder("SORTIERE 1 abc", false);
    checkOrder("SORTIERE VOR abcdefg", false);
    checkOrder("SORTIERE VOR abc 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SpioniereReader}.
   */
  @Test
  public void testSpioniereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SPY) + " abc");
    checkOrder("SPIONIERE 123");
    checkOrder("SPIONIERE 123 123", false);
    checkOrder("SPIONIERE", false);
    checkOrder("SPIONIERE ALLES", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.StirbReader}.
   */
  @Test
  public void testStirbReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_QUIT) + " \"abc\"");
    checkOrder("STIRB", false);
    checkOrder("STIRB 123", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TarneReader}.
   */
  @Test
  public void testTarneReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_HIDE));
    checkOrder("TARNE 0");
    checkOrder("TARNE 1");
    checkOrder("TARNE NICHT", false);
    checkOrder("TARNE xyz", false);
    checkOrder("TARNE 0 1", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TransportiereReader}.
   */
  @Test
  public void testTransportiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CARRY) + " zwei");
    checkOrder("TRANSPORTIERE zwei NICHT", false);
    checkOrder("TRANSPORTIERE NICHT", false);
    checkOrder("TRANSPORTIERE zwei zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.TreibeReader}.
   */
  @Test
  public void testTreibeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_TAX));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UnterhalteReader}.
   */
  @Test
  public void testUnterhalteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ENTERTAIN));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.UrsprungReader}.
   */
  @Test
  public void testUrsprungReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_ORIGIN) + " 1 1");
    checkOrder("URSPRUNG 1", false);
    checkOrder("URSPRUNG 1 2 3", false);
    checkOrder("URSPRUNG 2,3", false);
    checkOrder("URSPRUNG a b", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VergesseReader}.
   */
  @Test
  public void testVergesseReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_FORGET) + " Hiebwaffen");
    checkOrder("VERGESSE", false);
    checkOrder("VERGESSE Tuten", false);
    checkOrder("VERGESSE Hiebwaffen Ausdauer", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.VerkaufeReader}.
   */
  @Test
  public void testVerkaufeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SELL) + " 1 Balsam");
    checkOrder("VERKAUFE ALLES Balsam");
    checkOrder("VERKAUFE Balsam", false);
    checkOrder("VERKAUFE 2 3 Balsam", false);
    checkOrder("VERKAUFE Balsam Balsam", false);
    checkOrder("VERKAUFE 2", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.FinalKeywordReader}.
   */
  @Test
  public void testFinalKeywordReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_LEAVE));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZaubereReader}.
   */
  @Test
  public void testZaubereReader() {
    GameDataBuilder.addSpells(data);

    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_CAST) + " \"Gro�es Fest\"");

    checkOrder("ZAUBERE STUFE 2 Schild zwei");
    checkOrder("ZAUBERE STUFE 2 \"Gro�es Fest\"");
    checkOrder("ZAUBERE Schild NICHT", true); // TODO cave: NICHT is read as spell parameter
    checkOrder("ZAUBERE \"Gro�es Fest\" NICHT", false);
    checkOrder("ZAUBERE Hagel", false); // combat spell
    checkOrder("ZAUBERE STUFE Schild zwei", false);
    checkOrder("ZAUBERE Magisches Gescho�", false);
    checkOrder("ZAUBERE \"Gro�es Fest\" zwei", false);
    checkOrder("ZAUBERE STUFE x Schild zwei", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZeigeReader}.
   */
  @Test
  public void testZeigeReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SHOW) + " Schild");
    checkOrder("ZEIGE ALLES ZAUBER");
    checkOrder("ZEIGE ALLES Tr�nke");
    checkOrder("ZEIGE Zwerg");
    checkOrder("ZEIGE Schild");
    checkOrder("ZEIGE ALLES", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZerstoereReader}.
   */
  @Test
  public void testZerstoereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_DESTROY));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.ZuechteReader}.
   */
  @Test
  public void testZuechteReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_GROW) + " PFERDE");
    checkOrder("Z�CHTE KR�UTER");
    checkOrder("Z�CHTE 2 KR�UTER");
    checkOrder("Z�CHTE 2", false);
    checkOrder("Z�CHTE 2 3", false);
    checkOrder("Z�CHTE Flachwurz 2", false);
    checkOrder("Z�CHTE Flachwurz", false);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser.SabotiereReader}.
   */
  @Test
  public void testSabotiereReader() {
    checkOrder(Resources.getOrderTranslation(EresseaConstants.O_SABOTAGE) + " SCHIFF");
    checkOrder("SABOTIERE", false);
    checkOrder("SABOTIERE SCHIFF ship", false);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getHandlers(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testGetHandlers() {
    List<OrderHandler> list = getParser().getHandlers(new OrderToken("a"));
    assertTrue(list != null);
    if (list == null)
      return;
    assertTrue(list.size() == 2);
    assertTrue(list.get(0).getClass().equals(ArbeiteReader.class));
    assertTrue(list.get(1).getClass().equals(AttackReader.class));
    list = getParser().getHandlers(new OrderToken("arbei"));
    assertTrue(list != null);
    if (list == null)
      return;
    assertTrue(list.size() == 1);
    list = getParser().getHandlers(new OrderToken("aga"));
    assertTrue(list != null);
    if (list == null)
      return;
    assertTrue(list.size() == 0);
  }

  /**
   * Null token should be matched by no handler
   */
  @Test(expected = NullPointerException.class)
  public void testGetHandlersNull() {
    getParser().getHandlers(null);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#readDescription()}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescription() {
    getParser().read(new StringReader("a \"abc\""));
    assertNotNull(getParser().readDescription());
    getParser().read(new StringReader("\"abc\""));
    assertEquals(null, getParser().readDescription());
    getParser().read(new StringReader("a \"\""));
    assertEquals("", getParser().readDescription());
    getParser().read(new StringReader("a \"\" a"));
    assertEquals(null, getParser().readDescription());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(boolean)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescriptionBoolean() {
    getParser().read(new StringReader("a \"abc\""));
    assertNotNull(getParser().readDescription(true));
    getParser().read(new StringReader("\"abc\""));
    assertEquals(null, getParser().readDescription(true));
    getParser().read(new StringReader("a \"\""));
    assertNotNull(getParser().readDescription(true));
    getParser().read(new StringReader("a \"\""));
    assertEquals(null, getParser().readDescription(false));
    getParser().read(new StringReader("a \"\" a"));
    assertEquals(null, getParser().readDescription(true));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testReadDescriptionOrderToken() {

  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readDescription(magellan.library.utils.OrderToken, boolean)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadDescriptionOrderTokenBoolean() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\" \"abc\""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc ; abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, true));
    getParser().read(new StringReader("abc ; abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"abc\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, false));
    getParser().read(new StringReader("\"abc\"; 123"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"abc\"; 123"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, false));
    getParser().read(new StringReader("\"\""));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"\""));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
    getParser().read(new StringReader("\"\";abc"));
    token = getParser().getLastToken();
    assertNotNull(getParser().readDescription(token, true));
    getParser().read(new StringReader("\"\";abc"));
    token = getParser().getLastToken();
    assertEquals(null, getParser().readDescription(token, false));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalKeyword(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalKeyword() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalKeyword(token));
    assertTrue(token.ttype == OrderToken.TT_KEYWORD);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalString(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalString() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("abc ;123"));
    token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader("\"abc abc\""));
    token = getParser().getLastToken();
    assertTrue(getParser().readFinalString(token));
    getParser().read(new StringReader("\"abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalString(token));
    getParser().read(new StringReader("1234#"));
    token = getParser().getLastToken();
    try {
      getParser().readFinalString(token);
      assertFalse(true);
    } catch (IllegalArgumentException e) {
      // should throw exception!
    }
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalID(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalID() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalID(token));
    assertTrue(token.ttype == OrderToken.TT_ID);

    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalID(token));
    assertTrue(token.ttype == OrderToken.TT_ID);

    getParser().read(new StringReader("")); // TODO what should happen here?
    token = getParser().getLastToken();
    try {
      getParser().readFinalID(token);
      fail("should throw exception");
    } catch (NoSuchElementException e) {
      // okay
    }
    assertTrue(token.ttype == OrderToken.TT_ID);
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#readFinalNumber(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testReadFinalNumber() {
    getParser().read(new StringReader("abc"));
    OrderToken token = getParser().getLastToken();
    assertTrue(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
    getParser().read(new StringReader("abc abc"));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
    getParser().read(new StringReader(""));
    token = getParser().getLastToken();
    assertFalse(getParser().readFinalNumber(token));
    assertTrue(token.ttype == OrderToken.TT_NUMBER);
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#checkNextFinal()}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testCheckNextFinal() {
    getParser().read(new StringReader("abc"));
    assertTrue(getParser().checkNextFinal());
    getParser().read(new StringReader(""));
    assertFalse(getParser().checkNextFinal());
    getParser().read(new StringReader("; abc"));
    assertFalse(getParser().checkNextFinal());
    getParser().read(new StringReader("abc; abc"));
    assertTrue(getParser().checkNextFinal());
    getParser().read(new StringReader("abc abc"));
    assertFalse(getParser().checkNextFinal());
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#checkFinal(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testCheckFinal() {
    assertTrue(getParser().checkFinal(new OrderToken(OrderToken.TT_EOC)));
    assertTrue(getParser().checkFinal(new OrderToken(OrderToken.TT_COMMENT)));
    assertFalse(getParser().checkFinal(new OrderToken(OrderToken.TT_STRING)));
    getParser().read(new StringReader(""));
    assertTrue(getParser().checkFinal(getParser().getLastToken()));
    getParser().read(new StringReader("; abc"));
    assertTrue(getParser().checkFinal(getParser().getLastToken()));
    getParser().read(new StringReader("abc; abc"));
    assertFalse(getParser().checkFinal(getParser().getLastToken()));
    assertTrue(getParser().checkFinal(getParser().getNextToken()));
    getParser().read(new StringReader("abc"));
    assertFalse(getParser().checkFinal(getParser().getLastToken()));
    assertTrue(getParser().checkFinal(getParser().getNextToken()));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#unexpected(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testUnexpected() {
    assertTrue(getParser().getErrorMessage() == null);
    getParser().setErrMsg("error");
    assertTrue(getParser().getErrorMessage().equals("error"));
    getParser().setErrMsg(null);
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("ARBEITEN"));
    assertTrue(getParser().getErrorMessage() == null);
    getParser().read(new StringReader("ARBEITEN 2"));
    assertTrue(getParser().getErrorMessage().equals(
        "Unexpected token 2: Undefined(9, 10), not followed by Space"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isNumeric(java.lang.String, int, int, int)}
   * .
   */
  @Test
  public void testIsNumericStringIntIntInt() {

  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isNumeric(java.lang.String)}.
   */
  @Test
  public void testIsNumericString() {
    assertTrue(getParser().isNumeric("123456567"));
    assertTrue(getParser().isNumeric("-2", 10, -10, 0));
    assertTrue(getParser().isNumeric("abc", 36, 13368, 13368));
    assertTrue(getParser().isNumeric("ff", 16, 0, 256));
    assertFalse(getParser().isNumeric("ff", 16, 0, 100));
    assertFalse(getParser().isNumeric("-2"));
    assertFalse(getParser().isNumeric("1 2"));
    assertFalse(getParser().isNumeric("1,2"));
    assertFalse(getParser().isNumeric("1.2"));
    assertFalse(getParser().isNumeric("--"));
    assertFalse(getParser().isNumeric("a"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isID(java.lang.String)}.
   */
  @Test
  public void testIsID() {
    assertTrue(getParser().isID("TEMP abc"));
    assertFalse(getParser().isID("TEMP abc", false));
    assertTrue(getParser().isID("12"));
    assertTrue(getParser().isID("abc"));
    assertTrue(getParser().isID("2ac"));
    assertFalse(getParser().isID("12345"));
    assertFalse(getParser().isID("1,3"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isTempID(java.lang.String)}.
   */
  @Test
  public void testIsTempID() {
    assertTrue(getParser().isTempID("TEMP abc"));
    assertTrue(getParser().isTempID("TEMP 1"));
    assertFalse(getParser().isTempID("1,3"));
    assertFalse(getParser().isTempID("abc"));
    assertFalse(getParser().isTempID(" TEMP abc "));
    assertFalse(getParser().isTempID(" TEMP TEMP temp"));
  }

  /**
   * Test method for {@link magellan.library.gamebinding.EresseaOrderParser#isRID(java.lang.String)}
   * .
   */
  @Test
  public void testIsRID() {
    assertTrue(getParser().isRID("1,3"));
    assertTrue(getParser().isRID("1,-3"));
    assertTrue(getParser().isRID("-1,-3323"));
    assertTrue(getParser().isRID("1,3,4"));
    assertFalse(getParser().isRID("1, 3"));
    assertFalse(getParser().isRID(" 1,3"));
    assertFalse(getParser().isRID("1,3 "));
    assertFalse(getParser().isRID("1,, 3"));
    assertFalse(getParser().isRID("1 3"));
    assertFalse(getParser().isRID("1 -3"));
    assertFalse(getParser().isRID("-a, 1"));
    assertFalse(getParser().isRID("123"));
    assertFalse(getParser().isRID("a, b"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsQuoted() {
    assertFalse(getParser().isQuoted("abc"));
    assertTrue(getParser().isQuoted("\"abc\""));
    assertFalse(getParser().isQuoted("'abc'"));
    assertFalse(getParser().isQuoted("abc5d"));
    assertFalse(getParser().isQuoted("567"));
    assertFalse(getParser().isQuoted("\"abc'"));
    assertFalse(getParser().isQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSingleQuoted(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsSingleQuoted() {
    assertFalse(getParser().isSingleQuoted("abc"));
    assertFalse(getParser().isSingleQuoted("\"abc\""));
    assertTrue(getParser().isSingleQuoted("'abc'"));
    assertFalse(getParser().isSingleQuoted("abc5d"));
    assertFalse(getParser().isSingleQuoted("567"));
    assertFalse(getParser().isSingleQuoted("\"abc'"));
    assertFalse(getParser().isSingleQuoted("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isSimpleString(java.lang.String)}.
   */
  @Test
  public void testIsSimpleString() {
    assertTrue(getParser().isSimpleString("abc"));
    assertFalse(getParser().isSimpleString("\"abc\""));
    assertFalse(getParser().isSimpleString("'abc'"));
    assertTrue(getParser().isSimpleString("abc5d"));
    assertFalse(getParser().isSimpleString("567"));
    assertFalse(getParser().isSimpleString("\"abc'"));
    assertFalse(getParser().isSimpleString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(java.lang.String)}.
   */
  @SuppressWarnings("deprecation")
  @Test
  public void testIsStringString() {
    assertTrue(getParser().isString("abc"));
    assertTrue(getParser().isString("\"abc\""));
    assertTrue(getParser().isString("'abc'"));
    assertTrue(getParser().isString("abc5d"));
    assertFalse(getParser().isString("567"));
    assertFalse(getParser().isString("\"abc'"));
    assertFalse(getParser().isString("'123"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken)}
   * .
   */
  @Test
  public void testIsStringOrderToken() {
    OrderToken token = new OrderToken("");
    assertTrue(getParser().isString(token, false) == getParser().isString(token));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    assertTrue(getParser().isString(token, false) == getParser().isString(token));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isString(magellan.library.utils.OrderToken, boolean)}
   * .
   */
  @Test
  public void testIsStringOrderTokenBoolean() {
    OrderToken token = new OrderToken("");
    assertFalse(getParser().isString(token, true));
    assertFalse(getParser().isString(token, false));
    token = new OrderToken("a");
    assertFalse(getParser().isString(token, true));
    assertTrue(getParser().isString(token, false));
    token = new OrderToken(OrderToken.TT_OPENING_QUOTE);
    assertTrue(getParser().isString(token, true));
    assertTrue(getParser().isString(token, false));
    token = new OrderToken("'abc");
    assertFalse(getParser().isString(token, true));
    assertFalse(getParser().isString(token, false));
    token = new OrderToken("5");
    assertFalse(getParser().isString(token, true));
    assertFalse(getParser().isString(token, false));
    token = new OrderToken(OrderToken.TT_EOC);
    assertTrue(getParser().isString(token, true));
    assertTrue(getParser().isString(token, false));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#getString(magellan.library.utils.OrderToken)}
   * .
   */
  @SuppressWarnings({ "deprecation", "null" })
  @Test
  public void testGetString() {
    getParser().read(new StringReader("abc"));
    OrderToken[] result = getParser().getString(getParser().getLastToken());
    assertTrue(result[0] == null);
    OrderToken contentToken = new OrderToken("abc", 0, 3, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    assertTrue(result[2] == null);
    OrderToken nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("@"));
    result = getParser().getString(getParser().getLastToken());
    assertTrue(result[0] == null);
    contentToken = new OrderToken("@", 0, 1, OrderToken.TT_STRING, false);
    assertTrue(result[1] == null);
    assertTrue(result[2] == null);

    getParser().read(new StringReader("\"abc\""));
    result = getParser().getString(getParser().getLastToken());
    OrderToken openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    OrderToken closingToken = new OrderToken("\"", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("'abc'"));
    result = getParser().getString(getParser().getLastToken());
    openingToken = new OrderToken("'", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("abc", 1, 4, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = new OrderToken("'", 4, 5, OrderToken.TT_CLOSING_QUOTE, false);
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("\"a"));
    result = getParser().getString(getParser().getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a", 1, 2, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

    getParser().read(new StringReader("\"a'"));
    result = getParser().getString(getParser().getLastToken());
    openingToken = new OrderToken("\"", 0, 1, OrderToken.TT_OPENING_QUOTE, false);
    assertTrue(equals(result[0], openingToken));
    contentToken = new OrderToken("a'", 1, 3, OrderToken.TT_STRING, false);
    assertTrue(result[1] != null && equals(result[1], contentToken));
    closingToken = null;
    assertTrue(equals(result[2], closingToken));
    nextToken = new OrderToken(OrderToken.TT_EOC);
    assertTrue(equals(result[3], nextToken));

  }

  protected boolean equals(OrderToken orderToken, OrderToken nextToken) {
    if (orderToken == null || nextToken == null)
      return orderToken == nextToken;
    return orderToken.getText().equals(nextToken.getText())
        && orderToken.getStart() == nextToken.getStart()
        && orderToken.getEnd() == nextToken.getEnd() && orderToken.ttype == nextToken.ttype
        && orderToken.followedBySpace() == nextToken.followedBySpace();
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithoutTemps() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN 123 456 678"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(5));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("123"));
    assertThat(bucket.get(2).getText(), is("456"));
    assertThat(bucket.get(3).getText(), is("678"));
    assertTrue(new OrderToken(OrderToken.TT_EOC).equalsAll(bucket.get(4)));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithTemp() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(3));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithTwoTemps() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123 TEMP 456"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(4));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
    assertThat(bucket.get(2).getText(), is("TEMP 456"));
  }

  /** Test method for {@link EresseaOrderParser.TokenBucket#mergeTempTokens(int)}. */
  @Test
  public void testMergeTempTokensWithMixed() {
    TokenBucket bucket = getParser().new TokenBucket();
    bucket.read(new StringReader("LEHREN TEMP 123 456 TEMP abc"));
    bucket.mergeTempTokens(36);
    assertThat(bucket.size(), is(5));
    assertThat(bucket.get(0).getText(), is("LEHREN"));
    assertThat(bucket.get(1).getText(), is("TEMP 123"));
    assertThat(bucket.get(2).getText(), is("456"));
    assertThat(bucket.get(3).getText(), is("TEMP abc"));
  }

  /**
   * Test method for
   * {@link magellan.library.gamebinding.EresseaOrderParser#isEmailAddress(java.lang.String)}.
   */
  @Test
  public void testIsEmailAddress() {
    assertTrue(getParser() != null);
    if (getParser() != null) {
      assertTrue(getParser().isEmailAddress("a@b.com"));
      assertTrue(getParser().isEmailAddress("123@234.com"));
      assertTrue(getParser().isEmailAddress("a.b.c.defg.a@hallo.bla.bla.com"));
      assertFalse(getParser().isEmailAddress(""));
      assertFalse(getParser().isEmailAddress("a"));
      assertFalse(getParser().isEmailAddress("a.b"));
      assertFalse(getParser().isEmailAddress("@b"));
      assertFalse(getParser().isEmailAddress("@b.com"));
      assertTrue(getParser().isEmailAddress("jsmith@[192.168.2.1]"));
      // FIXME these tests fail
      if (DO_KNOWN_FAILURES) {
        assertFalse(getParser().isEmailAddress(".@.")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress("a@b")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress("a.@b.com")); // shouldn't be allowed, but is
        assertFalse(getParser().isEmailAddress(".a@b.com")); // shouldn't be allowed, but is
        // shouldn't be allowed, but is
        assertTrue(getParser().isEmailAddress("\"!#$%&'*+-/=?^_`{|}~\"@example.com"));
      }
    }
  }

  /**
   * Returns the value of parser.
   * 
   * @return Returns parser.
   */
  protected EresseaOrderParser getParser() {
    return parser;
  }

  /**
   * Sets the value of parser.
   * 
   * @param parser The value for parser.
   */
  protected void setParser(EresseaOrderParser parser) {
    this.parser = parser;
  }

  /**
   * Returns the value of completer.
   * 
   * @return Returns completer.
   */
  protected EresseaOrderCompleter getCompleter() {
    return completer;
  }

  /**
   * Sets the value of completer.
   * 
   * @param completer The value for completer.
   */
  protected void setCompleter(EresseaOrderCompleter completer) {
    this.completer = completer;
  }

}
