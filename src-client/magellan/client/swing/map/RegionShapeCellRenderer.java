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

package magellan.client.swing.map;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;

import magellan.client.MagellanContext;
import magellan.client.desktop.Initializable;
import magellan.client.swing.context.ContextChangeable;
import magellan.client.swing.context.ContextObserver;
import magellan.client.swing.preferences.PreferencesAdapter;
import magellan.client.utils.Colors;
import magellan.library.Faction;
import magellan.library.GameData;
import magellan.library.Region;
import magellan.library.StringID;
import magellan.library.Unit;
import magellan.library.event.GameDataEvent;
import magellan.library.event.GameDataListener;
import magellan.library.rules.RegionType;
import magellan.library.utils.Resources;
import magellan.library.utils.logging.Logger;

/**
 * This renderer draws plain one-color hex fields. It supports three modes - the Regiontype-Mode,
 * the Politics-Mode and the All-Factions-Mode. In Regiontype-Mode a region is rendered with a color
 * based on its type while in Politics Mode the color is based on the faction reigning over the
 * region. In All-Factions-Mode the colors of all factions in this region are used.
 */
public class RegionShapeCellRenderer extends AbstractRegionShapeCellRenderer implements
    Initializable, ContextChangeable, GameDataListener {
  private static final Logger log = Logger.getInstance(RegionShapeCellRenderer.class);

  /**
   * The (REGION) tag in the cr that is used to define a color, the tag value is expected to be a
   * string like #00ff00
   */
  public static final String MAP_TAG = "mapcolor";

  /** The default value for factionKey. */
  public static final String DEFAULT_FACTION_KEY = "GeomRenderer.FactionColors";

  /** The default value for regionKey. */
  public static final String DEFAULT_REGION_KEY = "GeomRenderer.RegionColors";

  /** The default value for paintKey. */
  public static final String DEFAULT_PAINTMODE_KEY = "GeomRenderer.paintMode";

  /** Defines the RegionType paint mode. */
  public static final int PAINT_REGIONTYPE = 0;

  /** Defines the Politics paint mode. */
  public static final int PAINT_POLITICS = 1;

  /** Defines the All Factions paint mode. */
  public static final int PAINT_ALLFACTIONS = 2;

  /** Defines the Trust Level OVERALL paint mode. */
  public static final int PAINT_TRUST_OVERALL = 3;

  /** Defines the Trust Level GUARD paint mode. */
  public static final int PAINT_TRUST_GUARD = 4;

  /** Stores the current painting mode. */
  protected int paintMode;

  /** Used for storing all found factions. */
  private List<String> factions = new LinkedList<String>();

  /** Stores the faction - color pairs. The key is the faction name (Faction.getName()) */
  protected Map<String, Color> factionColors;

  /** The ocean is rendered with this color in Politics Mode. */
  protected Color oceanColor;

  /** All regions which can't be assigned to a faction are rendered with this color. */
  protected Color unknownColor;

  /** Stores the region - color paires. The key is the region type name (RegionType.getName()). */
  protected Map<StringID, Color> regionColors;

  /** The PreferencesAdapter used for changing colors. */
  protected GeomRendererAdapter adapter;

  /** The default key for settings operations with the faction-color table. */
  protected String factionKey;

  /** The default key for settings operations with the region-color table. */
  protected String regionKey;

  /** The default key for settings operations with the Politics Mode variable. */
  protected String paintKey;

  /** A default color for regions without type. */
  public static Color typelessColor = new Color(128, 128, 128);
  protected JMenu contextMenu;
  protected ContextObserver obs;
  protected Color singleColorArray[] = new Color[1];

  // private Random random;

  // private Color lastColor;

  /**
   * Constructs a new RegionShapeCellRenderer object using the given geometry and settings. The
   * default settings-operations keys are initialized to:
   * <ul>
   * <li>factionKey="GeomRenderer.FactionColors"</li>
   * <li>regionKey="GeomRenderer.RegionColors"</li>
   * <li>paintKey="GeomRenderer.paintMode"</li>
   * </ul>
   * 
   * @param geo The CellGeometry to be used
   * @param context The Properties to be used
   */
  public RegionShapeCellRenderer(CellGeometry geo, MagellanContext context) {
    this(geo, context, RegionShapeCellRenderer.DEFAULT_FACTION_KEY,
        RegionShapeCellRenderer.DEFAULT_REGION_KEY, RegionShapeCellRenderer.DEFAULT_PAINTMODE_KEY);
  }

  /**
   * Constructs a new RegionShapeCellRenderer object using the given geometry and settings. The
   * given Strings fKey, rKey and pKey are used to initialize the default settings-operations keys.
   * 
   * @param geo The CellGeometry to be used
   * @param context The Properties to be used
   * @param fKey The factionKey value for settings operations
   * @param rKey The regionKey value for settings operations
   * @param pKey The paintKey value for settings operations
   */
  public RegionShapeCellRenderer(CellGeometry geo, MagellanContext context, String fKey,
      String rKey, String pKey) {
    super(geo, context);

    factionColors = new HashMap<String, Color>();
    regionColors = new HashMap<StringID, Color>();

    // use this keys for default load/save operations
    factionKey = fKey;
    regionKey = rKey;
    paintKey = pKey;

    // initialize faction colors
    loadFactionColors(false);
    loadOceanColor();
    loadUnknownColor();

    // initialize region colors
    initDefaultRegionTypeColors(); // load default as base
    loadRegionColors(false); // add the settings-colors

    try {
      setPaintMode(Integer.parseInt(settings.getProperty(paintKey)));
    } catch (Exception exc) {
      setPaintMode(0);
    }

    initContextMenu();

    context.getEventDispatcher().addGameDataListener(this);
    // random = new Random(System.currentTimeMillis() / 10000);
  }

  /**
   * Puts default values for some region-types to the region-color table. Currently the following
   * region-types are implemented:
   * <ul>
   * <li>Ebene</li>
   * <li>Sumpf</li>
   * <li>Wald</li>
   * <li>W�ste</li>
   * <li>Ozean</li>
   * <li>Feuerwand</li>
   * <li>Hochebene</li>
   * <li>Berge</li>
   * <li>Gletscher</li>
   * </ul>
   */
  protected void initDefaultRegionTypeColors() {
    regionColors.put(StringID.create("Ebene"), new Color(49, 230, 0));
    regionColors.put(StringID.create("Sumpf"), new Color(0, 180, 160));
    regionColors.put(StringID.create("Wald"), new Color(36, 154, 0));
    regionColors.put(StringID.create("W�ste"), new Color(244, 230, 113));
    regionColors.put(StringID.create("Ozean"), new Color(128, 128, 255));
    regionColors.put(StringID.create("Feuerwand"), new Color(255, 79, 0));
    regionColors.put(StringID.create("Hochebene"), new Color(166, 97, 56));
    regionColors.put(StringID.create("Berge"), new Color(155, 145, 141));
    regionColors.put(StringID.create("Gletscher"), new Color(222, 226, 244));
  }

  // //////////////////
  // Access methods //
  // //////////////////

  /**
   * Returns the current Politics Mode.
   * 
   * @return the current mode
   */
  public int getPaintMode() {
    return paintMode;
  }

  /**
   * Sets the Paint Mode of this renderer to pm. The new setting is stored with the paintKey in the
   * global settings.
   * 
   * @param mode The new mode
   */
  public void setPaintMode(int mode) {
    paintMode = mode;
    settings.setProperty(paintKey, String.valueOf(paintMode));
  }

  /**
   * Changes the color of faction f to color c.
   * 
   * @param f The faction the color is for
   * @param c The (new) color
   */
  public void setFactionColor(Faction f, Color c) {
    setFactionColor(f.getName(), c);
  }

  /**
   * Changes the color of the faction with title name to color c.
   * 
   * @param name The faction name the color is for
   * @param c The (new) color
   */
  public void setFactionColor(String name, Color c) {
    if (c == null) {
      if (factionColors.remove(name) != null) {
        saveFactionColors();
      }
    } else {
      Color oldC = factionColors.get(name);

      if (!c.equals(oldC)) {
        factionColors.put(name, c);
        saveFactionColors();
      }
    }
  }

  /**
   * Puts a whole map to the faction-colors Table. Keys of this map should be factions or strings.
   * 
   * @param map A map containing Faction/String - Color pairs
   */
  public void setFactionColors(Map<?, Color> map) {
    Iterator<?> it = map.keySet().iterator();

    while (it.hasNext()) {
      Object o = it.next();

      if (o instanceof Faction) {
        factionColors.put(((Faction) o).getName(), map.get(o));
      }

      if (o instanceof String) {
        factionColors.put((String) o, map.get(o));
      }
    }

    saveFactionColors();
  }

  /**
   * Changes the color of region-type r to color c.
   * 
   * @param r The RegionType the color is for
   * @param c The (new) color
   */
  public void setRegionColor(RegionType r, Color c) {
    setRegionColor(r.getID(), c);
  }

  /**
   * Changes the color of the region-type with title name to color c.
   * 
   * @param name The RegionType name the color is for
   * @param c The (new) color
   */
  public void setRegionColor(StringID name, Color c) {
    Color oldC = regionColors.get(name);

    if (!c.equals(oldC)) {
      regionColors.put(name, c);
      saveRegionColors();
    }
  }

  /**
   * Puts a whole map to the region-colors Table. Keys should be region-types or strings.
   * 
   * @param map A map containig RegionType/String - Color pairs
   */
  public void setRegionColors(Map<Object, Color> map) {
    for (Object o : map.keySet()) {
      if (o instanceof RegionType) {
        regionColors.put(((RegionType) o).getID(), map.get(o));
      }

      if (o instanceof String) {
        regionColors.put(StringID.create((String) o), map.get(o));
      }
    }

    saveRegionColors();
  }

  /**
   * Returns the colors used for ocean regions in Politics Mode.
   * 
   * @return the ocean color
   */
  public Color getOceanColor() {
    return oceanColor;
  }

  /**
   * Sets the color used for ocean fields in Politics Mode and stores it in the global settings.
   * 
   * @param c The new ocean color
   */
  public void setOceanColor(Color c) {
    oceanColor = c;
    saveOceanColor();
  }

  /**
   * Returns the colors used for unassigned regions in Politics Mode.
   * 
   * @return The color for unassigned regions
   */
  public Color getUnknownColor() {
    return unknownColor;
  }

  /**
   * Sets the color used for unassigned fields in Politics Mode and stores it in the global
   * settings.
   * 
   * @param c The new color for unassigned regions
   */
  public void setUnknownColor(Color c) {
    unknownColor = c;
    saveUnknownColor();
  }

  /**
   * Returns the default key for settings operations concerning the faction-colors table.
   * 
   * @return the faction-color table key
   */
  public String getFactionKey() {
    return factionKey;
  }

  /**
   * Sets the key used for default settings operations concerning the faction colors.
   * 
   * @param fKey the new key
   */
  public void setFactionKey(String fKey) {
    factionKey = fKey;
  }

  /**
   * Returns the default key for settings operations concerning the region-colors table.
   * 
   * @return the region-color table key
   */
  public String getRegionKey() {
    return regionKey;
  }

  /**
   * Sets the key used for deafult settings operations concerning the region colors.
   * 
   * @param rKey the new key
   */
  public void setRegionKey(String rKey) {
    regionKey = rKey;
  }

  /**
   * Returns the default key for settings operations concerning the mode of this renderer.
   * 
   * @return the Politics Mode key
   */
  public String getPaintKey() {
    return paintKey;
  }

  /**
   * Sets the key used for deafult settings operations concerning the Politics Mode.
   * 
   * @param pKey the new key
   */
  public void setPaintKey(String pKey) {
    paintKey = pKey;
  }

  // //////////////////////////////
  // Global settings operations //
  // //////////////////////////////
  protected void load(Map<String, Color> dest, String key, boolean reset) {
    load(dest, key, reset, String.class);
  }

  /**
   * Parses the global settings value with the given key into the hashtable dest. If the reset flag
   * is set, the hashtable is cleared before parsing.
   * 
   * @param dest The destination hashtable
   * @param key the settings key to use
   * @param reset true if the hashtable should be cleared
   */
  protected <T> void load(Map<T, Color> dest, String key, boolean reset, Class<T> keyType) {
    if (reset) {
      dest.clear();
    }

    String colors = settings.getProperty(key);

    if (colors == null)
      return;

    StringTokenizer st = new StringTokenizer(colors, ";");

    try {
      while (st.hasMoreTokens()) {
        String name = st.nextToken();
        String red = st.nextToken();
        String green = st.nextToken();
        String blue = st.nextToken();

        T mkey;
        // i.pavkovic: stay conform with Colors.decode
        if (keyType.equals(String.class)) {
          mkey = (T) name;
        } else if (keyType.equals(StringID.class)) {
          mkey = (T) StringID.create(name);
        } else {
          RegionShapeCellRenderer.log.error("unknown key type");
          return;
        }

        dest.put(mkey, Colors.decode(red + "," + green + "," + blue));
      }
    } catch (ClassCastException exc) {
      RegionShapeCellRenderer.log.error(exc);
      exc.printStackTrace();
    } catch (Exception exc) {
      RegionShapeCellRenderer.log.warn(exc);
    }

    // maybe not the right count
  }

  /**
   * Writes the given hashtable into the global settings using the given key.
   * 
   * @param source The source hashtable with name-Color pairs
   * @param key The key for the property to set
   */
  protected void save(Map<?, Color> source, String key) {
    StringBuffer buffer = new StringBuffer();
    Iterator<?> it = source.keySet().iterator();

    while (it.hasNext()) {
      Object name = it.next();
      Color col = source.get(name);
      buffer.append(name);
      buffer.append(';');
      buffer.append(Colors.encode(col).replace(',', ';'));

      if (it.hasNext()) {
        buffer.append(';');
      }
    }

    settings.setProperty(key, buffer.toString());
  }

  /**
   * Loads the faction colors saved with the default key factionKey. If the reset flag is set, the
   * color-table is cleared before loading.
   * 
   * @param reset true if the faction-colors should be resetted
   */
  public void loadFactionColors(boolean reset) {
    loadFactionColors(factionKey, reset);
  }

  /**
   * Loads the faction colors saved with the given key. If the reset flag is set, the color-table is
   * cleared before loading.
   * 
   * @param key The key for the settings to load from
   * @param reset true if the faction colors should be reset
   */
  public void loadFactionColors(String key, boolean reset) {
    load(factionColors, key, reset);
  }

  /**
   * Saves the faction colors into the global settings using the default key factionKey.
   */
  public void saveFactionColors() {
    saveFactionColors(factionKey);
  }

  /**
   * Saves the faction colors into the global settings using the given key.
   * 
   * @param key The property to save to
   */
  public void saveFactionColors(String key) {
    save(factionColors, key);
  }

  /**
   * Loads the region colors saved with the default key regionKey. If the reset flag is set, the
   * color-table is cleared before loading.
   * 
   * @param reset true if the region colors should be resetted
   */
  public void loadRegionColors(boolean reset) {
    loadRegionColors(regionKey, reset);
  }

  /**
   * Loads the region colors saved with the given key. If the reset flag is set, the color-table is
   * cleared before loading.
   * 
   * @param key the property to load from
   * @param reset true if the region colors should be resetted
   */
  public void loadRegionColors(String key, boolean reset) {
    load(regionColors, key, reset, StringID.class);
  }

  /**
   * Saves the faction colors into the global settings using the default key regionKey.
   */
  public void saveRegionColors() {
    saveRegionColors(regionKey);
  }

  /**
   * Saves the region colors into the global settings using the given key.
   * 
   * @param key the property to save to
   */
  public void saveRegionColors(String key) {
    save(regionColors, key);
  }

  /**
   * Loads the ocean color used for ocean fields in Politics Mode. The key for this is
   * "GeomRenderer.OceanColor".
   */
  public void loadOceanColor() {
    String color = settings.getProperty("GeomRenderer.OceanColor", "128,128,255");

    // replace old ";" with ","
    oceanColor = Colors.decode(color.replace(';', ','));
  }

  /**
   * Saves the ocean color using the key "GeomRenderer.OceanColor".
   */
  public void saveOceanColor() {
    settings.setProperty("GeomRenderer.OceanColor", Colors.encode(oceanColor));
  }

  /**
   * Loads the unknown color used for unassigned fields in Politics Mode. The key for this is
   * "GeomRenderer.UnknownColor".
   */
  public void loadUnknownColor() {
    String color = settings.getProperty("GeomRenderer.UnknownColor", "128,128,128");

    // replace old ";" with ","
    unknownColor = Colors.decode(color.replace(';', ','));
  }

  /**
   * Saves the unknown color using the key "GeomRenderer.UnknownColor".
   */
  public void saveUnknownColor() {
    settings.setProperty("GeomRenderer.UnknownColor", Colors.encode(unknownColor));
  }

  // ////////////////////////////////////
  // Rendering color decision methods //
  // ////////////////////////////////////
  @Override
  protected Color[] getColor(Region r) {
    if (paintMode != RegionShapeCellRenderer.PAINT_ALLFACTIONS)
      return null;

    Color col = getTagColor(r);

    if (col != null) {
      singleColorArray[0] = col;

      return singleColorArray;
    }

    factions.clear();

    Iterator<Unit> it = r.units().iterator();

    while (it.hasNext()) {
      String f = (it.next()).getFaction().getName();

      if (!factions.contains(f)) {
        factions.add(f);
      }
    }

    if (factions.size() == 0) {
      singleColorArray[0] = getUnknownColor();

      if (r.getRegionType().isOcean()) {
        singleColorArray[0] = getOceanColor();
      }

      return singleColorArray;
    }

    if (factions.size() == 1) {
      singleColorArray[0] = getFactionColor(factions.get(0));

      return singleColorArray;
    }

    Color cols[] = new Color[factions.size()];

    for (int i = 0; i < cols.length; i++) {
      cols[i] = getFactionColor(factions.get(i));
    }

    return cols;
  }

  @Override
  protected Color getSingleColor(Region r) {
    Color col = getTagColor(r);

    if (col != null)
      return col;

    switch (paintMode) {
    case PAINT_REGIONTYPE:
      return getRegionTypeColor(r.getRegionType());

    case PAINT_POLITICS:
      return getPoliticsColor(r);

    case PAINT_ALLFACTIONS:
      return getUnknownColor(); // this is an error !!!

    case PAINT_TRUST_OVERALL:
      return getTrustColor(r, false);

    case PAINT_TRUST_GUARD:
      return getTrustColor(r, true);

    default:
      return null; // error
    }
  }

  /**
   * If the region has a tag with the key {@link #MAP_TAG}, its value is interpreted as a color,
   * encoded like #00ff00 (see {@link Color#decode(String)}).
   * 
   * @return A color or <code>null</code> if the region has no color tag
   */
  protected Color getTagColor(Region r) {
    if (r.containsTag(RegionShapeCellRenderer.MAP_TAG)) {
      String s = r.getTag(RegionShapeCellRenderer.MAP_TAG);

      if (s.length() > 0) {
        if (s.charAt(0) == '#') {
          try {
            return Color.decode(s);
          } catch (NumberFormatException nfe) {
            // bad tag, ignore
          }
        }

        return Colors.decode(s);
      }
    }

    return null;
  }

  /**
   * Fills the cell polygon if in TrustLevel mode. Fallback is Politics mode. After that left side
   * is filled with lowest over all trustlevel, right side with lowest trustlevel of guarding units.
   */
  protected Color getTrustColor(Region r, boolean guard) {
    Color col = null;

    if (guard) {
      // find lowest guarding trust
      col = getLowestGuardingTrustColor(r);
    } else {
      // find lowest over all trust
      col = getLowestTrustColor(r);
    }

    // fallback
    if (col == null) {
      col = getPoliticsColor(r);
    }

    return col;
  }

  protected Color getLowestTrustColor(Region r) {
    int minLevel = Integer.MAX_VALUE;
    Faction minFaction = null;
    Iterator<Unit> it = r.units().iterator();

    while (it.hasNext()) {
      Unit u = it.next();

      if (u.getFaction().getTrustLevel() < minLevel) {
        minFaction = u.getFaction();
        minLevel = minFaction.getTrustLevel();
      }
    }

    if (minFaction == null)
      return null;

    return getFactionColor(minFaction, Color.red);
  }

  protected Color getLowestGuardingTrustColor(Region r) {
    int minLevel = Integer.MAX_VALUE;
    Faction minFaction = null;
    Iterator<Unit> it = r.units().iterator();

    while (it.hasNext()) {
      Unit u = it.next();

      if ((u.getGuard() != 0) && (u.getFaction().getTrustLevel() < minLevel)) {
        minFaction = u.getFaction();
        minLevel = minFaction.getTrustLevel();
      }
    }

    if (minFaction == null)
      return null;

    return getFactionColor(minFaction, Color.red);
  }

  /**
   * Returns the color of a region type. If the type can't be found in the region-color table, a new
   * random color entry is put.
   * 
   * @param type the region type for which a color is needed
   * @return the color for the given type
   */
  protected Color getRegionTypeColor(RegionType type) {
    if (type == null)
      return RegionShapeCellRenderer.typelessColor;

    if (!regionColors.containsKey(type.getID())) {
      Color c =
          new Color((int) (Math.random() * 128) + 128, (int) (Math.random() * 128) + 128,
              (int) (Math.random() * 128) + 128);
      setRegionColor(type.getID(), c);

      // adapter.addColor(0,type,c);
    }

    return regionColors.get(type.getID());
  }

  /**
   * Returns the color of a region depending on the faction which own this region. The current
   * implementation first looks if the type of this region is "Ozean" in which case the ocean color
   * is used. Then it checks for an explicitly set owner unit. Normally the owner of the biggest
   * "Burg" is returned. If there's no "Burg", the people of the different factions are counted and
   * the faction with most people "wins". If this algorithm does not supply a faction, the unknown
   * color is used.
   * 
   * @param r The region for which a color is needed
   * @return the color for the given region
   */
  protected Color getPoliticsColor(Region r) {
    Unit unit = r.getOwnerUnit();

    if (unit != null)
      return getFactionColor(unit.getFaction());

    Faction f = RegionShapeCellRenderer.getMaxPeopleFaction(r);

    if (f != null)
      return getFactionColor(f);

    if (r.getRegionType().isOcean())
      return oceanColor;

    return unknownColor;
  }

  /**
   * Returns the color for faction f. If there's no entry in the faction-color table a new
   * random-color entry is created.
   * 
   * @param f the faction for which a color is needed
   * @return the color for the given faction
   */
  protected Color getFactionColor(Faction f) {
    return getFactionColor(f.getName());
  }

  /**
   * Returns the color for faction with name f. If there's no entry in the faction-color table a new
   * random-color entry is created.
   * 
   * @param f the faction for which a color is needed
   * @return the color for the given faction
   */
  protected Color getFactionColor(String f) {
    if (f == null) {
      f = "null";
    }

    if (!factionColors.containsKey(f)) {
      int i = 0;
      Color c = createColor(f, i);
      while ((distance(c, oceanColor) < 160 || distance(c, unknownColor) < 160) && i++ < 50) {
        c = createColor(f, i);
      }
      setFactionColor(f, c);
    }

    // adapter.addColor(1,f,c);
    return factionColors.get(f);
  }

  private int distance(Color c1, Color c2) {
    return (c1.getRed() - c2.getRed()) * (c1.getRed() - c2.getRed())
        + (c1.getGreen() - c2.getGreen()) * (c1.getGreen() - c2.getGreen())
        + (c1.getBlue() - c2.getBlue()) * (c1.getBlue() - c2.getBlue());
  }

  /**
   * Create a random color that is different from the last created color.
   */
  private Color createColor(String name, int seed) {
    final int min = 100, max = 241, diff = max - min;
    Color c =
        new Color(min + (Math.abs(name.hashCode()) + 31 * seed) % diff, min
            + (Math.abs(name.hashCode() >> 6) + 31 * seed) % diff, min
            + (Math.abs(hashCode() >> 12) + 31 * seed) % diff);
    // lastColor = c;
    return c;
  }

  // /**
  // * Create a random color that is different from the last created color.
  // */
  // private Color createColor() {
  // final int min = 100, max = 240, diff = max - min;
  // if (lastColor == null) {
  // lastColor = new Color(min, min, min);
  // }
  // int offsR = lastColor.getRed() - min;
  // int offsG = lastColor.getGreen() - min;
  // int offsB = lastColor.getBlue() - min;
  // switch (random.nextInt(3)) {
  // case 0:
  // offsR += diff / 3 + 1;
  // break;
  // case 1:
  // offsG += diff / 3 + 1;
  // break;
  // case 2:
  // offsB += diff / 3 + 1;
  // break;
  // }
  // Color c =
  // new Color((offsR + random.nextInt(diff / 4 + 1)) % diff + min, (offsG + random.nextInt(25))
  // % diff + min, (offsB + random.nextInt(25)) % diff + min);
  // lastColor = c;
  // return c;
  // }

  /**
   * Returns the color for faction f. If there's no entry in the faction-color table the entry is
   * created with given color.
   * 
   * @param f the faction for which a color is needed
   * @param c the default color
   * @return the color for the given faction
   */
  protected Color getFactionColor(Faction f, Color c) {
    return getFactionColor(f.getName(), c);
  }

  /**
   * Returns the color for faction with name f. If there's no entry in the faction-color table the
   * entry is created with given color.
   * 
   * @param f the faction for which a color is needed
   * @param c the default color
   * @return the color for the given faction
   */
  protected Color getFactionColor(String f, Color c) {
    if (!factionColors.containsKey(f)) {
      setFactionColor(f, c);

      // adapter.addColor(1,f,c);
    }

    return factionColors.get(f);
  }

  /**
   * Counts the people of all faction in the given region and returns the faction with most of them,
   * or null if there are no units.
   * 
   * @param r A region to count
   * @return the faction with most people or null if there are no units
   */
  public static Faction getMaxPeopleFaction(Region r) {
    Faction maxFaction = null;
    int maxPeople = -1;
    Iterator<Unit> it = r.units().iterator();

    while (it.hasNext()) {
      Unit unit = it.next();
      Faction curFaction = unit.getFaction();
      Iterator<Unit> intern = r.units().iterator();
      int curPeople = 0;

      while (intern.hasNext()) {
        Unit unit2 = intern.next();

        if (unit2.getFaction().equals(curFaction)) {
          curPeople += unit2.getPersons();
        }
      }

      if (curPeople > maxPeople) {
        maxPeople = curPeople;
        maxFaction = curFaction;
      }
    }

    return maxFaction;
  }

  /**
   * Just returns the current mode number.
   */
  public java.lang.String getComponentConfiguration() {
    return String.valueOf(paintMode);
  }

  /**
   * Tries to convert the string to a number and set the corresponding paint mode.
   */
  public void initComponent(java.lang.String p1) {
    try {
      setPaintMode(Integer.parseInt(p1));
    } catch (NumberFormatException nfe) {
      // ignore format error
    }
  }

  protected void initContextMenu() {
    contextMenu = new JMenu(getName());

    ActionListener al = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setPaintMode(Integer.parseInt(e.getActionCommand()));

        if (obs != null) {
          obs.contextDataChanged();
        }
      }
    };

    JMenuItem item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.terrain"));
    item.addActionListener(al);
    item.setActionCommand("0");
    contextMenu.add(item);

    item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.byfaction"));
    item.addActionListener(al);
    item.setActionCommand("1");
    contextMenu.add(item);

    item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.allfactions"));
    item.addActionListener(al);
    item.setActionCommand("2");
    contextMenu.add(item);

    item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevel"));
    item.addActionListener(al);
    item.setActionCommand("3");
    contextMenu.add(item);

    item = new JMenuItem(Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevelandguard"));
    item.addActionListener(al);
    item.setActionCommand("4");
    contextMenu.add(item);
  }

  /**
   * @see magellan.client.swing.context.ContextChangeable#getContextAdapter()
   */
  public JMenuItem getContextAdapter() {
    return contextMenu;
  }

  /**
   * @see magellan.client.swing.context.ContextChangeable#setContextObserver(magellan.client.swing.context.ContextObserver)
   */
  public void setContextObserver(ContextObserver co) {
    obs = co;
  }

  /**
   * @see magellan.client.swing.map.HexCellRenderer#getName()
   */
  @Override
  public String getName() {
    return Resources.get("map.regionshapecellrenderer.name");
  }

  /**
   * Invoked when the current game data object becomes invalid.
   */
  public void gameDataChanged(GameDataEvent e) {
    GameData newData = e.getGameData();

    initFactionColors(newData);

    initRegionColors(newData);
    data = newData;
  }

  protected void initRegionColors(GameData newData) {
    for (Region r : newData.getRegions()) {
      getRegionTypeColor(r.getRegionType());
    }
  }

  protected void initFactionColors(GameData newData) {
    // check for new factions
    for (Faction f : newData.getFactions()) {

      // generates a new one if not present
      if (f.getTrustLevel() <= Faction.TL_DEFAULT) {
        getFactionColor(f, Color.red);
      } else {
        getFactionColor(f);
      }
    }
  }

  /**
   * Returns the PreferencesAdapter this renderer uses.
   * 
   * @return A GeomRendererAdapter instance
   */
  @Override
  public PreferencesAdapter getPreferencesAdapter() {
    /*
     * if (adapter==null) adapter=new GeomRendererAdapter(); return adapter;
     */
    return new GeomRendererAdapter();
  }

  /**
   * A PreferencesAdapter for use with RegionShapeCellRenderer objects. It supplies two cards for
   * both RegionType and Politics Mode. The cards are initialized at creation time. Updates are
   * performed through calls of addColor(). Changes are applied directly.
   */
  protected class GeomRendererAdapter extends JPanel implements PreferencesAdapter, ActionListener {
    private class ModePanel extends JPanel implements ActionListener {
      /**
       * The element for showing in the list. Encapsulates name and color plus an id(not necessary)
       */
      private class ListElement {
        String name;
        // StringID id;
        Color color;

        ListElement(String n, StringID i, Color c) {
          name = n;
          // id = i;
          color = c;
        }
      }

      // /////////////////
      // Some comparators for list elements
      // /////////////////
      /**
       * Compares (factions) by name
       */
      private class NameComparator implements Comparator<ListElement> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(ListElement o1, ListElement o2) {
          return o1.name.compareTo(o2.name);
        }
      }

      /**
       * Compares (factions) by trust level.
       */
      private class ListElementFactionTrustComparator implements Comparator<ListElement> {
        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(ListElement o1, ListElement o2) {
          String name1 = o1.name;
          String name2 = o2.name;

          if (name1 == null) {
            name1 = "null";
          }

          if (name2 == null) {
            name2 = "null";
          }

          // try to find the factions in a data object
          if (data != null) {
            Faction f1 = null;
            Faction f2 = null;
            Iterator<? extends Faction> it = data.getFactions().iterator();

            while (it.hasNext() && ((f1 == null) || (f2 == null))) {
              Faction f = it.next();

              if (name1.equals(f.getName())) {
                f1 = f;
              }

              if (name2.equals(f.getName())) {
                f2 = f;
              }
            }

            if (f1 == null) {
              if (f2 == null)
                return nameComp.compare(o1, o2);

              return 1;
            } else {
              if (f2 == null)
                return -1;

              if (f1.getTrustLevel() == f2.getTrustLevel())
                return nameComp.compare(o1, o2);

              return f2.getTrustLevel() - f1.getTrustLevel();
            }
          }

          return nameComp.compare(o1, o2);
        }

      }

      /**
       * Renders the list. Prefers ListElement values were the color is used for a color icon.
       */
      private class ModePanelCellRenderer extends JLabel implements ListCellRenderer {
        /**
         * Simple colored icon of given size(uses prefDim of ModePanel)
         */
        private class ColorIcon extends java.lang.Object implements javax.swing.Icon {
          private Color myColor;

          /**
           * Creates a new ColorIcon object.
           */
          public ColorIcon(Color c) {
            myColor = c;
          }

          /**
           * @see javax.swing.Icon#getIconHeight()
           */
          public int getIconHeight() {
            return prefDim.height;
          }

          /**
           * @see javax.swing.Icon#getIconWidth()
           */
          public int getIconWidth() {
            return prefDim.width;
          }

          /**
           * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
           */
          public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(myColor);
            g.fillRect(x, y, prefDim.width, prefDim.height);
          }

          /**
					 * 
					 */
          public void setColor(Color c) {
            myColor = c;
          }
        }

        ColorIcon icon;

        /**
         * Creates a new ModePanelCellRenderer object.
         */
        public ModePanelCellRenderer() {
          icon = new ColorIcon(Color.white);
          setIcon(icon);
          setOpaque(true);
        }

        /**
         * @see javax.swing.ListCellRenderer#getListCellRendererComponent(javax.swing.JList,
         *      java.lang.Object, int, boolean, boolean)
         */
        public Component getListCellRendererComponent(JList aList, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
          if (value instanceof ListElement) {
            ListElement elem = (ListElement) value;
            setText(elem.name);
            icon.setColor(elem.color);
          } else {
            icon.setColor(Color.white);
            setText("---");
          }

          setBackground(isSelected ? aList.getSelectionBackground() : aList.getBackground());
          setForeground(isSelected ? aList.getSelectionForeground() : aList.getForeground());

          return this;
        }
      }

      /**
       * A small ListModel implementation that allows ordering through a Comparator.
       */
      private class SortableListModel<T> extends AbstractListModel {
        protected List<T> listData;
        protected Comparator<T> comp = null;
        protected int listOffset = -1;

        /**
         * Creates a new SortableListModel object.
         */
        public SortableListModel(Collection<T> initData) {
          listData = new LinkedList<T>();

          if (initData != null) {
            listData.addAll(initData);
          }
        }

        /**
         * @see javax.swing.ListModel#getElementAt(int)
         */
        public Object getElementAt(int index) {
          return listData.get(index);
        }

        /**
         * @see javax.swing.ListModel#getSize()
         */
        public int getSize() {
          return listData.size();
        }

        /**
         * @param o
         */
        public void add(T o) {
          listData.add(o);
          fireIntervalAdded(this, listData.size() - 1, listData.size() - 1);

          if (comp != null) {
            sort(comp, listOffset);
          }
        }

        public void removeAll() {
          int old = listData.size();
          if (old > 0) {
            listData.clear();
            fireIntervalRemoved(this, 0, old - 1);
          }
        }

        public void remove(T o) {
          int index = listData.indexOf(o);
          if (index >= 0) {
            listData.remove(index);
            fireIntervalRemoved(this, index, index);
          }
        }

        public void contentChanged(int index) {
          fireContentsChanged(this, index, index);
        }

        /**
         * Sorts the list entries after offset by the provided comparator
         */
        public void sort(Comparator<T> aCmp, int aOffset) {
          this.comp = aCmp;
          this.listOffset = aOffset;

          try {
            Collection<T> front = null;

            if (aOffset > 0) {
              front = new LinkedList<T>();

              Iterator<T> it = listData.iterator();

              for (int i = 0; i < aOffset && it.hasNext(); ++i) {
                front.add(it.next());
                it.remove();
              }
            }

            Collections.sort(listData, aCmp);

            if (aOffset > 0) {
              listData.addAll(0, front);
            }

            fireContentsChanged(this, Math.max(0, aOffset), listData.size() - 1);
          } catch (Exception exc) {
            log.warn(exc);
          }
        }
      }

      @SuppressWarnings("hiding")
      private final Logger log = Logger.getInstance(ModePanel.class);
      protected boolean politicsMode;
      private Collection<Object> myColors;
      private Dimension prefDim;
      private JList list;
      private SortableListModel<ListElement> listModel;
      private String oceanLabel;
      private String unknownLabel;
      private Comparator<ListElement> nameComp;
      private Comparator<ListElement> trustComp;
      private JCheckBox nameBox;
      private JCheckBox trustBox;
      private JButton removeButton;

      /**
       * Creates a new ModePanel object.
       */
      public ModePanel(boolean politics) {
        politicsMode = politics;

        oceanLabel = Resources.get("map.regionshapecellrenderer.lbl.ocean.caption");
        unknownLabel = Resources.get("map.regionshapecellrenderer.lbl.unassignedregion.caption");

        nameComp = new NameComparator();
        trustComp = new ListElementFactionTrustComparator();

        listModel = new SortableListModel<ListElement>(null);
        myColors = new LinkedList<Object>();

        prefDim = new Dimension(50, 10);

        list = new JList(listModel);
        list.setCellRenderer(new ModePanelCellRenderer());

        final JList mList = list;
        list.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
              int index = mList.locationToIndex(e.getPoint());

              if (index >= 0) {
                actionPerformed(index);
              }
            }
          }
        });

        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(list);
        // list gets too wide if we have very long faction names so...
        scrollPane.setPreferredSize(new Dimension(150, 100));
        this.add(scrollPane, BorderLayout.CENTER);

        if (politicsMode) {
          JPanel left = new JPanel(magellan.client.swing.CenterLayout.SPAN_X_LAYOUT);
          Box box = Box.createVerticalBox();
          ButtonGroup group = new ButtonGroup();
          nameBox =
              new JCheckBox(Resources.get("map.regionshapecellrenderer.chk.compare.name"), true);
          nameBox.addActionListener(this);
          group.add(nameBox);
          box.add(nameBox);

          trustBox =
              new JCheckBox(Resources.get("map.regionshapecellrenderer.chk.compare.trust"), false);
          trustBox.addActionListener(this);
          group.add(trustBox);
          box.add(trustBox);
          left.add(box);
          this.add(left, BorderLayout.WEST);

          removeButton =
              new JButton(Resources.get("map.regionshapecellrenderer.btn.remove.faction"));
          removeButton.addActionListener(this);
          this.add(removeButton, BorderLayout.SOUTH);
        }
        loadElements(politicsMode ? factionColors : regionColors);
      }

      protected void addGUIPair(Object obj, Color col) {
        String name = null;
        StringID id = null;

        if (obj instanceof RegionType) {
          RegionType rt = (RegionType) obj;
          name = rt.getName();
          id = rt.getID();
        } else if (obj instanceof StringID) {
          id = (StringID) obj;
          name = id.toString();
        } else {
          if (obj != null) {
            name = obj.toString();
          } else {
            name = "null";
          }
        }

        if (name == null) {
          log.info(obj);
        }

        ListElement element = new ListElement(name, id, col);
        listModel.add(element);
      }

      protected void loadElements(Map<?, Color> source) {
        listModel.removeAll();
        if (politicsMode) { // add ocean and unknown
          addGUIPair(oceanLabel, oceanColor);
          addGUIPair(unknownLabel, unknownColor);
        }

        // if we get IDs we have to get the object behind
        Iterator<?> it = source.keySet().iterator();

        while (it.hasNext()) {
          Object key = it.next();

          if (!politicsMode) {
            // we work with regionColors, so key is a StringID
            StringID id = (StringID) key;

            if ((data != null) && (data.getRules() != null)) {
              RegionType rt = data.getRules().getRegionType(id);

              if (rt != null) {
                key = rt;
              }
            }
          }

          Color c = source.get(key);
          addGUIPair(key, c);
          myColors.add(key);
        }

        if (nameBox != null) {
          listModel.sort(nameBox.isSelected() ? nameComp : trustComp, politicsMode ? 2 : 0);
        }
        list.repaint();
      }

      /**
       * Adds a name/color pair to the list
       */
      public void addColor(String name, Color c) {
        if (name == null) {
          name = "null";
        }

        if (!myColors.contains(name)) {
          addGUIPair(name, c);
          myColors.add(name);
        }
      }

      /**
       * Add a region type/color pair to the list
       */
      public void addColor(RegionType name, Color c) {
        if (!myColors.contains(name)) {
          addGUIPair(name, c);
          myColors.add(name);
        }
      }

      public void removeColor(String name) {
        for (int i = 0; i < listModel.getSize(); ++i) {
          ListElement element = (ListElement) listModel.getElementAt(i);
          if (element.name.equals(name)) {
            listModel.remove(element);
            myColors.remove(name);
          }
        }
      }

      protected void actionPerformed(int index) {
        ListElement element = (ListElement) listModel.getElementAt(index);

        String title = null;
        if (oceanLabel.equals(element.name)) {
          title = Resources.get("map.regionshapecellrenderer.dialog.oceancolor.title");
        } else if (unknownLabel.equals(element.name)) {
          title = Resources.get("map.regionshapecellrenderer.dialog.unassignedregioncolor.title");
        } else {
          Object msgArgs[] = { element.name };
          title =
              (new java.text.MessageFormat(Resources
                  .get("map.regionshapecellrenderer.dialog.color.title"))).format(msgArgs);
        }

        Color c = JColorChooser.showDialog(parent, title, element.color);

        if (c != null) {
          element.color = c;
          listModel.contentChanged(index);
        }
      }

      /**
       * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
       */
      public void actionPerformed(ActionEvent e) {
        // sort from third because of ocean und unknown
        if (e.getSource() == nameBox) {
          listModel.sort(nameComp, 2);
          list.repaint();
        } else if (e.getSource() == trustBox) {
          listModel.sort(trustComp, 2);
          list.repaint();
        } else if (e.getSource() == removeButton) {
          ListElement element = (ListElement) list.getSelectedValue();
          if (element != null
              && JOptionPane.showConfirmDialog(this, Resources.get(
                  "map.regionshapecellrenderer.dialog.confirmdelete.message", element.name, list
                      .getSelectedIndices().length), Resources
                  .get("map.regionshapecellrenderer.dialog.confirmdelete.title"),
                  JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            for (Object val : list.getSelectedValues()) {
              element = (ListElement) val;
              if (!oceanLabel.equals(element.name) && !unknownLabel.equals(element.name)) {
                removeColor(element.name);
              }
            }
          }
        }
      }

      /**
       * Returns the mapping currently represented by this panel.
       */
      public Map<String, Color> getElements() {
        Map<String, Color> result = new HashMap<String, Color>();
        for (int index = 0; index < listModel.getSize(); ++index) {
          ListElement element = (ListElement) listModel.getElementAt(index);
          result.put(element.name, element.color);
        }
        return result;
      }

    }

    /** A dummy frame for Dialogs */
    private JFrame parent; // there should be another way

    /** The panel which holds the cards */
    protected JPanel inner;

    /** The layout which manages the mode cards */
    protected CardLayout card;

    /** The box for the two modes */
    private JComboBox modeBox;

    /** The two ModePanels for RegionType and Politics Mode */
    private ModePanel content[];

    /**
     * Creates a new GeomRendererAdapter for the super class.
     */
    public GeomRendererAdapter() {
      parent = new JFrame();

      inner = new JPanel();
      inner.setLayout(card = new CardLayout());
      inner.setBorder(new javax.swing.border.TitledBorder(BorderFactory.createEtchedBorder(),
          Resources.get("map.regionshapecellrenderer.border.colortable")));
      content = new ModePanel[2];
      content[0] = new ModePanel(false);
      content[1] = new ModePanel(true);

      JScrollPane scroller = new JScrollPane(content[0]);
      scroller.getVerticalScrollBar().setUnitIncrement(16);
      inner.add(scroller, "0");
      scroller = new JScrollPane(content[1]);
      scroller.getVerticalScrollBar().setUnitIncrement(16);
      inner.add(scroller, "1");

      String items[] = new String[5];
      items[0] = Resources.get("map.regionshapecellrenderer.cmb.mode.terrain");
      items[1] = Resources.get("map.regionshapecellrenderer.cmb.mode.byfaction");
      items[2] = Resources.get("map.regionshapecellrenderer.cmb.mode.allfactions");
      items[3] = Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevel");
      items[4] = Resources.get("map.regionshapecellrenderer.cmb.mode.trustlevelandguard");
      modeBox = new JComboBox(items);
      modeBox.setEditable(false);
      modeBox.addActionListener(this);

      JPanel modePanel = new JPanel();
      modePanel
          .add(new JLabel(Resources.get("map.regionshapecellrenderer.lbl.rendermode.caption")));
      modePanel.add(modeBox);

      setLayout(new GridBagLayout());
      JTextArea description =
          HexCellRenderer.createDescriptionPanel(Resources
              .get("map.regionshapecellrenderer.description"), this);
      GridBagConstraints con =
          new GridBagConstraints(0, 0, 1, 1, 1, 1, GridBagConstraints.NORTHWEST,
              GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
      con.weighty = 0.1;
      add(description, con);

      con.gridy++;
      con.weighty = 1;
      add(modePanel, con);
      con.gridy++;
      add(inner, con);

      // initColors();
      showCard(getPaintMode());
    }

    /**
     * Adds a new pair to the indexed ModePanel.
     * 
     * @param index the ModePanel to use - 0 for RegionType, 1 for Politics
     * @param name the name of the new pair
     * @param c the color of the new pair
     */
    public void addColor(int index, String name, Color c) {
      content[index].addColor(name, c);
    }

    /**
     * @see #addColor(int, String, Color)
     */
    public void addColor(int index, RegionType name, Color c) {
      content[index].addColor(name, c);
    }

    /**
     * Remove a color mapping from the indexed ModePanel
     * 
     * @see #addColor(int, String, Color)
     */
    public void removeColor(int index, String name) {
      content[index].removeColor(name);
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#initPreferences()
     */
    public void initPreferences() {
      content[0].loadElements(regionColors);
      content[1].loadElements(factionColors);
      showCard(getPaintMode());
    }

    /**
     * @see magellan.client.swing.preferences.PreferencesAdapter#applyPreferences()
     */
    public void applyPreferences() {
      factionColors.clear();
      regionColors.clear();
      for (int mode = 0; mode < 2; ++mode) {
        for (Map.Entry<String, Color> entry : content[mode].getElements().entrySet()) {
          if (content[1].oceanLabel.equals(entry.getKey())) {
            setOceanColor(entry.getValue());
          } else if (content[1].unknownLabel.equals(entry.getKey())) {
            setUnknownColor(entry.getValue());
          } else if (mode == 1) {
            setFactionColor(entry.getKey(), entry.getValue());
          } else if (mode == 0) {
            setRegionColor(StringID.create(entry.getKey()), entry.getValue());
          }
        }
      }
      setPaintMode(modeBox.getSelectedIndex());
      // settings.setProperty(paintKey, String.valueOf(paintMode));
      if (data != null) {
        initFactionColors(data);
        initRegionColors(data);
      }
      saveFactionColors();
      saveRegionColors();
      saveOceanColor();
      saveUnknownColor();
    }

    /**
     * Returns the component for graphical display.
     * 
     * @return the GUI component
     * @see magellan.client.swing.preferences.PreferencesAdapter#getComponent()
     */
    public Component getComponent() {
      return this;
    }

    /**
     * Returns the title of this adapter.
     * 
     * @return the adapter title
     */
    public String getTitle() {
      return Resources.get("map.regionshapecellrenderer.name");
    }

    /**
     * Called when the mode box changes its selection. Shows the corresponding card.
     * 
     * @param p1 An action event
     */
    public void actionPerformed(java.awt.event.ActionEvent p1) {
      int s = modeBox.getSelectedIndex();

      switch (s) {
      case 0:
        card.first(inner);

        break;

      case 1:
      case 2:
      case 3:
      case 4:
        card.last(inner);

        break;
      }

    }

    /**
     * Shows the card with index c.
     * 
     * @param c 0 for regiontype, 1 for politics, 2 for all faction
     */
    public void showCard(int c) {
      modeBox.setSelectedIndex(c);
    }
  }
}
