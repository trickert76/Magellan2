// class magellan.client.preferences.Install4J
// created on Apr 18, 2022
//
// Copyright 2003-2022 by magellan project team
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
package magellan.client.preferences;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import magellan.library.utils.Resources;
import magellan.library.utils.VersionInfo;
import magellan.library.utils.logging.Logger;

/**
 * This class provides an interface to the install4j intaller and updater properties.
 *
 * @author stm
 * @version 1.0, Apr 18, 2022
 */
public class Install4J {
  private static final Logger log = Logger.getInstance(Install4J.class);
  private static final String SCHEDULE_KEY = "updateSchedule$Boolean";
  private static final String NIGHTLY_KEY = "updateMagellanNightly$Boolean";
  private static final String LAST_CHECK_KEY = "updateChecked";
  private File responseFile;
  private Properties install4jProps;
  protected String saved;
  private static Install4J instance;

  protected Install4J(File tBinDir) throws IOException {
    install4jProps = new Properties();
    if (tBinDir != null) {
      responseFile = new File(tBinDir, ".install4j");
      responseFile = new File(responseFile, "response.varfile");
      FileReader reader;
      log.fine("reading install4j configuration from " + responseFile.getAbsolutePath());
      install4jProps.load(reader = new FileReader(responseFile));
      reader.close();
      log.fine("done");
    } else {
      log.fine("not reading install4j configuration");
    }
  }

  /**
   * Initialize and read settings from ".install4j/response.varfile" in the given directory (the content directory).
   * This method must be called before any get... and set... methods may be called.
   * 
   * @return true if the file could be read.
   */
  public static boolean init(File directory) {
    try {
      instance = new Install4J(directory);
      return true;
    } catch (IOException e) {
      log.warn("could not read install4j response file", e);
      return false;
    }
  }

  private void save() throws IOException {
    store("install4j response file written by Magellan " + VersionInfo.getVersion(Resources.getResourceDirectory()));
  }

  /**
   * Store sorted by keys
   */
  protected void store(String comment) throws IOException {
    ByteArrayOutputStream buf;
    install4jProps.store(buf = new ByteArrayOutputStream(), comment);
    storeSorted(buf);
  }

  protected void storeSorted(ByteArrayOutputStream buf) throws IOException {
    LineNumberReader reader = new LineNumberReader(new InputStreamReader(new ByteArrayInputStream(buf.toByteArray())));
    String line;
    List<Object> lines = new ArrayList<Object>();
    while ((line = reader.readLine()) != null) {
      List<String> start = null;
      if (line.startsWith("#")) {
        start = new ArrayList<String>();
        start.add(line);
        while ((line = reader.readLine()) != null) {
          while (!line.startsWith("#") && line.matches("((\\\\\\\\)*\\\\)|(.*[^\\\\](\\\\\\\\)*\\\\)")) {
            String line2 = reader.readLine();
            line = line.substring(0, line.length() - 1) + line2.replaceFirst("^ *", "");
          }
          start.add(line);
          if (!line.startsWith("#")) {
            break;
          }
        }
      } else {
        // odd number of backslashes at the end: merge with following line, discarding leading spaces
        while (line.matches("((\\\\\\\\)*\\\\)|(.*[^\\\\](\\\\\\\\)*\\\\)")) {
          String line2 = reader.readLine();
          line = line.substring(0, line.length() - 1) + line2.replaceFirst("^ *", "");
        }
      }
      if (start != null) {
        lines.add(start);
      } else {
        lines.add(line);
      }

      // if (!line.startsWith("#")) {
      // // odd number of backslashes at the end: merge with following line, discarding leading spaces
      // while (line.matches("((\\\\\\\\)*\\\\)|(.*[^\\\\](\\\\\\\\)*\\\\)")) {
      // String line2 = reader.readLine();
      // line = line.substring(0, line.length() - 1) + line2.replaceFirst("^ *", "");
      // }
      // }
      // lines.add(line);
    }
    lines.sort((a, b) -> {
      String ca, cb;
      if (a instanceof String) {
        ca = (String) a;
      } else {
        List la = (List) a;
        ca = (String) la.get(la.size() - 1);
      }
      if (b instanceof String) {
        cb = (String) b;
      } else {
        List la = (List) b;
        cb = (String) la.get(la.size() - 1);
      }
      return ca.compareTo(cb);
      // if (a.startsWith("#") || b.startsWith("#"))
      // return 0;
      // else
      // return a.compareTo(b);
    });

    BufferedWriter writer2 = new BufferedWriter(new FileWriter(responseFile, Charset.forName("UTF-8")));
    for (Object l : lines) {
      if (l instanceof String) {
        writer2.write((String) l);
        writer2.newLine();
      } else {
        for (String ll : (List<String>) l) {
          writer2.write(ll);
          writer2.newLine();
        }
      }
    }
    writer2.close();
  }

  protected void store2(String comment) throws IOException {
    Writer writer;
    if (responseFile == null) {
      writer = new StringWriter();
    } else {
      writer = new FileWriter(responseFile);
    }

    Enumeration<Object> keys = install4jProps.keys();
    String[] keyA = new String[install4jProps.size()];
    for (int i = 0; keys.hasMoreElements(); ++i) {
      keyA[i] = keys.nextElement().toString();
    }
    Arrays.sort(keyA);

    if (comment != null) {
      writer.write("# ");
      writer.write(comment);
      writer.write("\n");
    }
    for (String key : keyA) {
      writer.write(escape(key));
      writer.write("=");
      writer.write(escape(install4jProps.getProperty(key)));
      writer.write("\n");
    }
    writer.close();
    if (responseFile == null) {
      saved = writer.toString();
    }
  }

  protected String escape(String input) {
    return input.replaceAll("([#\\\\=:!])", "\\\\$1");
  }

  private static boolean checkInstance() {
    if (instance == null) {
      log.warn("Install4J not initialized.");
      return false;
    }
    return true;
  }

  protected String getVariable(String key) {
    return install4jProps.getProperty(key);
  }

  protected String setVariable(String key, String value) {
    String old = install4jProps.getProperty(key);
    install4jProps.setProperty(key, value);
    try {
      log.finer("changing install4j property " + key + " to " + value);
      save();
    } catch (IOException e) {
      log.warn("could not write install4j settings", e);
      return null;
    }
    return old;
  }

  /**
   * Returns a boolean value for the given key from the install4j response file. Returns null if the key is not set.
   * 
   */
  public Boolean getBoolean(String key) {
    String val = instance.getVariable(key);
    if (val == null)
      return null;
    return "true".equals(val);
  }

  /**
   * Sets the boolean value for the given key. Returns the old value, or null if the key had no previous value.
   */
  public Boolean setBoolean(String key, boolean value) {
    String val = instance.setVariable(key, value ? "true" : "false");
    if (val == null)
      return null;
    return "true".equals(val);
  }

  /**
   * Returns a Long value for the given key from the install4j response file. Returns null if the key is not set.
   * 
   */
  public Long getLong(String key) {
    String val = instance.getVariable(key);
    if (val == null)
      return null;
    try {
      return Long.parseLong(val);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * Returns the update schedule. Current known values are ON_EVERY_START, DAILY, WEEKLY, MONTHLY, NEVER. Returns null
   * if no schedule is defined.
   */
  public static String getUpdateSchedule() {
    if (!checkInstance())
      return null;
    return instance.getVariable(SCHEDULE_KEY);
  }

  /**
   * Returns the time (as in {@link System#currentTimeMillis()} of the last successful update check of the install4j
   * updater. Returns null if there never was such a check.
   */
  public static Long lastUpdateCheck() {
    if (!checkInstance())
      return null;
    return instance.getLong(LAST_CHECK_KEY);
  }

  /**
   * Returns true if the nightly check was defined in the installer. Returns null if there is no known value.
   */
  public static Boolean isNightlyCheck() {
    if (!checkInstance())
      return null;
    return instance.getBoolean(NIGHTLY_KEY);
  }

  /**
   * Changes the nightly check property for the install4j updater.
   */
  public static Boolean setNightlyCheck(boolean selected) {
    if (!checkInstance())
      return null;
    return instance.setBoolean(NIGHTLY_KEY, selected);
  }

}
