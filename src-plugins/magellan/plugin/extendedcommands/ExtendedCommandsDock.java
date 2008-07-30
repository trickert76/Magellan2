// class magellan.plugin.extendedcommands.ExtendedCommandsDialog
// created on 02.02.2008
//
// Copyright 2003-2008 by magellan project team
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
package magellan.plugin.extendedcommands;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import magellan.client.desktop.MagellanDesktop;
import magellan.library.GameData;
import magellan.library.Unit;
import magellan.library.UnitContainer;
import magellan.library.utils.Resources;
import net.infonode.docking.DockingWindow;
import net.infonode.docking.DockingWindowListener;
import net.infonode.docking.OperationAbortedException;
import net.infonode.docking.View;
import net.infonode.tabbedpanel.Tab;
import net.infonode.tabbedpanel.TabDropDownListVisiblePolicy;
import net.infonode.tabbedpanel.TabbedPanel;
import net.infonode.tabbedpanel.titledtab.TitledTab;


/**
 * This is a dialog to edit the script/commands for a given unit.
 * 
 * TODO Save dialog positions (size, location, slider position)
 *
 * @author Thoralf Rickert
 * @version 1.0, 11.09.2007
 */
public class ExtendedCommandsDock extends JPanel implements ActionListener, DockingWindowListener {
  public static final String IDENTIFIER = "ExtendedCommands";
  private ExtendedCommands commands = null;
  private boolean visible = false;
  private TabbedPanel tabs = null;
  private GameData world = null;
  private Map<String, Tab> tabMap = new HashMap<String, Tab>();
  private Map<String, ExtendedCommandsDocument> docMap = new HashMap<String, ExtendedCommandsDocument>();
  
  public ExtendedCommandsDock(ExtendedCommands commands) {
    this.commands = commands;
    init();
  }
  
  protected void init() {
    setLayout(new BorderLayout());
    
    tabs = new TabbedPanel();
    tabs.getProperties().setTabReorderEnabled(true);
    tabs.getProperties().setTabDropDownListVisiblePolicy(TabDropDownListVisiblePolicy.TABS_NOT_VISIBLE);
    
    add(tabs,BorderLayout.CENTER);

    JPanel north = new JPanel();
    north.setLayout(new BoxLayout(north, BoxLayout.X_AXIS));
    north.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  
//    north.add(Box.createRigidArea(new Dimension(30, 0)));
    
//    JButton cancelButton = new JButton(Resources.get("button.cancel"));
//    cancelButton.setRequestFocusEnabled(false);
//    cancelButton.setActionCommand("button.cancel");
//    cancelButton.addActionListener(this);
//    cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
//    north.add(cancelButton);
//    
//    north.add(Box.createRigidArea(new Dimension(10, 0)));
    
    JButton executeButton = new JButton(Resources.get("button.execute"));
    executeButton.setRequestFocusEnabled(false);
    executeButton.setActionCommand("button.execute");
    executeButton.addActionListener(this);
    executeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(executeButton);

    north.add(Box.createRigidArea(new Dimension(5, 0)));
    
    JButton saveButton = new JButton(Resources.get("button.save"));
    saveButton.setRequestFocusEnabled(false);
    saveButton.setActionCommand("button.save");
    saveButton.addActionListener(this);
    saveButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
    north.add(saveButton);

    north.add(Box.createHorizontalGlue());
    add(north,BorderLayout.NORTH);
  }

  /**
   * This method is called, if one of the buttons is clicked.
   * 
   * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
   */
  public void actionPerformed(ActionEvent e) {
    
    
    if (e.getActionCommand().equalsIgnoreCase("button.execute")) {
      // let's get the tab and execute it inside the doc.
      Tab tab = tabs.getSelectedTab();
      if (tab == null) return; // we don't execute everything here....
      ExtendedCommandsDocument doc = (ExtendedCommandsDocument)tab.getContentComponent();
      doc.actionPerformed(e);
      
    } else if (e.getActionCommand().equalsIgnoreCase("button.save")) {
      // iterate thru all tabs and save the scripts
      for (int i=0; i<tabs.getTabCount(); i++) {
        Tab tab = tabs.getTabAt(i);
        if (tab == null) continue;
        ExtendedCommandsDocument doc = (ExtendedCommandsDocument)tab.getContentComponent();
        Script newScript = (Script)doc.getScript().clone();
        newScript.setScript(doc.getScriptingArea().getText());

        if (doc.getUnit() != null) {
          commands.setCommands(doc.getUnit(),newScript);
        } else if (doc.getContainer() != null) {
          commands.setCommands(doc.getContainer(),newScript);
        } else {
          commands.setLibrary(newScript);
        }
        
        doc.setModified(false);
      }
      commands.save();
      
//    } else if (e.getActionCommand().equalsIgnoreCase("button.cancel")) {
//      // just restore the old settings
//      if (isModified) {
//        int result = JOptionPane.showConfirmDialog(this, Resources.get("extended_commands.questions.not_saved"),Resources.get("extended_commands.questions.not_saved_title"),JOptionPane.OK_CANCEL_OPTION);
//        if (result != JOptionPane.OK_OPTION) return;
//      }
//
//      if (unit != null) {
//        commands.setCommands(unit, script); // reset to old script
//      } else if (container != null) {
//        commands.setCommands(container, script); // reset to old script
//      } else {
//        commands.setLibrary(script); // reset to old script
//      }
//      
//      if (script != null) {
//        scriptingArea.setText(script.getScript());
//        scriptingArea.setCaretPosition(script.getCursor());
//        priorityBox.setSelectedItem(script.getPriority());
//      }
//        
//      isModified = false;
    }
  }
  
  /**
   * Setups the dock and opens the script for the given unit or container.
   */
  public void setScript(Unit unit, UnitContainer container, Script script) {

    String key = createKey(unit,container);
    if (tabMap.containsKey(key)) {
      // ok, the entry already exists.
      tabs.setSelectedTab(tabMap.get(key));
    } else {
      // we have to create a tab (normal operation)
      ExtendedCommandsDocument doc = new ExtendedCommandsDocument();
      doc.setWorld(world);
      doc.setUnit(unit);
      doc.setContainer(container);
      doc.setScript(script);
      TitledTab tab = new TitledTab(key, null, doc, null);
      
      tabs.addTab(tab);
      tabs.setSelectedTab(tab);
      
      tabMap.put(key, tab);
      docMap.put(key, doc);
    }
    
    // Visibility
    if (!visible) {
      MagellanDesktop.getInstance().setVisible(ExtendedCommandsDock.IDENTIFIER, true);
    }
    
  }
  
  protected String createKey(Unit unit, UnitContainer container) {
    if (unit != null) return Resources.get("extended_commands.element.unit",unit.getName(),unit.getID()); 
    if (container != null) return Resources.get("extended_commands.element.container",container.getName(),container.getID());
    return Resources.get("extended_commands.element.library");
  }

  /**
   * Returns the value of world.
   * 
   * @return Returns world.
   */
  public GameData getWorld() {
    return world;
  }

  /**
   * Sets the value of world.
   *
   * @param world The value for world.
   */
  public void setWorld(GameData world) {
    this.world = world;
    // this means, we have to close all currently open tabs
    if (tabs != null && tabs.getTabCount()>0) {
      tabMap.clear();
      docMap.clear();
      
      for (int i=0; i<tabs.getTabCount(); i++) {
        tabs.removeTab(tabs.getTabAt(i));
        i--;
      }
    }
  }  
  /**
   * @see net.infonode.docking.DockingWindowListener#viewFocusChanged(net.infonode.docking.View, net.infonode.docking.View)
   */
  public void viewFocusChanged(View previouslyFocusedView, View focusedView) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowAdded(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowAdded(DockingWindow addedToWindow, DockingWindow addedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosed(net.infonode.docking.DockingWindow)
   */
  public void windowClosed(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowClosing(net.infonode.docking.DockingWindow)
   */
  public void windowClosing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocked(net.infonode.docking.DockingWindow)
   */
  public void windowDocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowDocking(net.infonode.docking.DockingWindow)
   */
  public void windowDocking(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowHidden(net.infonode.docking.DockingWindow)
   */
  public void windowHidden(DockingWindow window) {
    this.visible = false;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximized(net.infonode.docking.DockingWindow)
   */
  public void windowMaximized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMaximizing(net.infonode.docking.DockingWindow)
   */
  public void windowMaximizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimized(net.infonode.docking.DockingWindow)
   */
  public void windowMinimized(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowMinimizing(net.infonode.docking.DockingWindow)
   */
  public void windowMinimizing(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRemoved(net.infonode.docking.DockingWindow, net.infonode.docking.DockingWindow)
   */
  public void windowRemoved(DockingWindow removedFromWindow, DockingWindow removedWindow) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestored(net.infonode.docking.DockingWindow)
   */
  public void windowRestored(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowRestoring(net.infonode.docking.DockingWindow)
   */
  public void windowRestoring(DockingWindow window) throws OperationAbortedException {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowShown(net.infonode.docking.DockingWindow)
   */
  public void windowShown(DockingWindow window) {
    this.visible = true;
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocked(net.infonode.docking.DockingWindow)
   */
  public void windowUndocked(DockingWindow window) {
  }

  /**
   * @see net.infonode.docking.DockingWindowListener#windowUndocking(net.infonode.docking.DockingWindow)
   */
  public void windowUndocking(DockingWindow window) throws OperationAbortedException {
  }
}
