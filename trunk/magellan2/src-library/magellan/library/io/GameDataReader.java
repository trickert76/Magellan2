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

package magellan.library.io;

import java.io.IOException;
import java.io.Reader;

import magellan.library.CoordinateID;
import magellan.library.GameData;
import magellan.library.Rules;
import magellan.library.io.cr.CRParser;
import magellan.library.io.file.FileType;
import magellan.library.utils.NullUserInterface;
import magellan.library.utils.UserInterface;
import magellan.library.utils.logging.Logger;
import magellan.library.utils.transformation.IdentityTranslator;
import magellan.library.utils.transformation.ReportTranslator;
import magellan.library.utils.transformation.TwoLevelTranslator;

/**
 * The <code>GameDataReader</code> reads a <code>GameData</code> from a given <code>FileType</code>
 * 
 * @author $Author: $
 * @version $Revision: 302 $
 */
public class GameDataReader {
  private static final Logger log = Logger.getInstance(GameDataReader.class);

  protected UserInterface ui = null;

  /**
   * Constructs a new GameDataReader
   * 
   * @param ui The UserInterface for the progress. Can be NULL. Then no operation is displayed.
   */
  public GameDataReader(UserInterface ui) {
    if (ui == null) {
      ui = new NullUserInterface();
    }
    this.ui = ui;
  }

  /**
   * Read a gamedata from a given File. At the beginning the game name is read by a
   * <code>GameNameReader</code>. With this name the corresponding rules and game
   * 
   * @param aFileType the filetype representing a cr or xml file.
   * @return a GameData object read from the cr or xml file.
   * @throws IOException iff something went wrong while reading the file.
   */
  public GameData readGameData(FileType aFileType) throws IOException {
    return readGameData(aFileType, new IdentityTranslator());
  }

  /** @deprecated Use {@link #readGameData(FileType, ReportTranslator)} */
  @Deprecated
  public GameData readGameData(FileType aFileType, CoordinateID newOrigin) throws IOException {
    return readGameData(aFileType,
        new TwoLevelTranslator(newOrigin, CoordinateID.ZERO));
  }

  /**
   * Read a gamedata from a given File. At the beginning the game name is read by a
   * <code>GameNameReader</code>. With this name the corresponding rules and game can be parsed.
   * 
   * @param aFileType the filetype representing a cr or xml file.
   * @param newOrigin the loaded report is translated by this coordinates.
   * @return a GameData object read from the cr or xml file.
   * @throws IOException If an I/O error occurs
   */
  public GameData readGameData(FileType aFileType, ReportTranslator translator)
      throws IOException {
    // a) read game name
    String gameName = GameNameReader.getGameName(aFileType);

    if (gameName == null)
      throw new IOException("Unable to determine game name of file " + aFileType);

    return readGameData(aFileType, translator, gameName);
  }

  /**
   * @deprecated
   */
  @Deprecated
  public GameData readGameData(FileType aFileType, CoordinateID newOrigin, String gameName)
      throws IOException {
    return readGameData(aFileType,
        new TwoLevelTranslator(newOrigin, CoordinateID.ZERO), gameName);
  }

  /**
   * Read a gamedata from a given File.
   * 
   * @param aFileType the filetype representing a cr or xml file.
   * @param coordinateTranslator the loaded report is translated by this coordinates.
   * @param gameName
   * @return a GameData object read from the cr or xml file.
   * @throws IOException If an I/O error occurs
   */
  public GameData readGameData(FileType aFileType,
      ReportTranslator coordinateTranslator, String gameName) throws IOException {
    if (aFileType.isXMLFile()) {
      GameData data = readGameDataXML(aFileType, gameName, coordinateTranslator);

      if (data != null) {
        data.postProcess();
      }

      return data;
    }

    // TODO (stm) why do we need this?
    if (aFileType.isZIPFile() || aFileType.isGZIPFile() || aFileType.isBZIP2File()
        || aFileType.isCRFile()) {

      /*
       * readGameDataCR invokes method aFileType.createReader(). This method deals wih the different
       * treatment of different filetypes, hence we can simply say here
       * "all known cr types are treated the same" 20060917: Jonathan (Fiete)
       */
      GameData data = readGameDataCR(aFileType, gameName, coordinateTranslator);

      if (data != null) {
        data.postProcess();
      }

      return data;
    }

    throw new IOException("Don't know how to read unknown file format in " + aFileType);
  }

  /**
   * Reads game data from a XML file
   */
  protected GameData readGameDataXML(FileType aFileType, String aGameName) throws IOException {
    throw new IOException("Reading of xml files unfinished");
  }

  /**
   * Reads game data from a XML file
   */
  protected GameData readGameDataXML(FileType aFileType, String aGameName,
      ReportTranslator coordinateTranslator) throws IOException {
    throw new IOException("Reading of xml files unfinished");
  }

  /**
   * Reads the game data from a CR file
   */
  protected GameData readGameDataCR(FileType aFileType, String aGameName) throws IOException {
    return readGameDataCR(aFileType, aGameName, new IdentityTranslator());
  }

  /**
   * Reads the game data from a CR file
   * 
   * @param aFileType The CR file
   * @param aGameName
   * @param coordinateTranslator the loaded report is translated by this coordinates.
   * @return A new GameData object filled in with the information from the file.
   * @throws IOException If an I/O error occurs
   */
  protected GameData readGameDataCR(FileType aFileType, String aGameName,
      ReportTranslator coordinateTranslator) throws IOException {
    GameData newData = createGameData(aGameName);
    newData.setFileType(aFileType);

    Reader reader = aFileType.createReader();

    try {
      GameDataReader.log.info("Loading report " + aFileType.getName());
      CRParser parser = new CRParser(ui, coordinateTranslator);
      parser.read(reader, newData);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
      }
    }

    // after reading the filetype may be written
    aFileType.setReadonly(false);

    return newData;
  }

  /**
   * Creates a new GameData based on the gamename rules.
   * 
   * @param aGameName The game name whose rules are being read.
   * @return A new, empty GameData object
   * @throws IOException If an I/O error occurs or no rules could be found.
   */
  public GameData createGameData(String aGameName) throws IOException {
    Rules rules = new RulesReader().readRules(aGameName);

    if (rules == null)
      // This should never happen but who knows
      throw new IOException("No Rules for game '" + aGameName + "' readable!");

    return rules.getGameSpecificStuff().createGameData(aGameName);
  }

}
